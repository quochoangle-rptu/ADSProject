// ADS I Class Project
// Pipelined RISC-V Core
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/15/2023 by Tobias Jauch (@tojauch)

/*
The goal of this task is to implement a 5-stage pipeline that features a subset of RV32I (all R-type and I-type instructions). 

    Instruction Memory:
        The CPU has an instruction memory (IMem) with 4096 words, each of 32 bits.
        The content of IMem is loaded from a binary file specified during the instantiation of the MultiCycleRV32Icore module.

    CPU Registers:
        The CPU has a program counter (PC) and a register file (regFile) with 32 registers, each holding a 32-bit value.
        Register x0 is hard-wired to zero.

    Microarchitectural Registers / Wires:
        Various signals are defined as either registers or wires depending on whether they need to be used in the same cycle or in a later cycle.

    Processor Stages:
        The FSM of the processor has five stages: fetch, decode, execute, memory, and writeback.
        All stages are active at the same time and process different instructions simultaneously.

        Fetch Stage:
            The instruction is fetched from the instruction memory based on the current value of the program counter (PC).

        Decode Stage:
            Instruction fields such as opcode, rd, funct3, and rs1 are extracted.
            For R-type instructions, additional fields like funct7 and rs2 are extracted.
            Control signals (isADD, isSUB, etc.) are set based on the opcode and funct3 values.
            Operands (operandA and operandB) are determined based on the instruction type.

        Execute Stage:
            Arithmetic and logic operations are performed based on the control signals and operands.
            The result is stored in the aluResult register.

        Memory Stage:
            No memory operations are implemented in this basic CPU.

        Writeback Stage:
            The result of the operation (writeBackData) is written back to the destination register (rd) in the register file.

    Check Result:
        The final result (writeBackData) is output to the io.check_res signal.
        The exception signal is also passed to the wrapper module. It indicates whether an invalid instruction has been encountered.
        In the fetch stage, a default value of 0 is assigned to io.check_res.
*/

package core_tile

import chisel3._
import chisel3.util._


class PipelinedRV32Icore (BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    val check_res = Output(UInt(32.W))  // Result for verification
    val exception = Output(Bool())       // Exception flag
  })

  // =========================================================================
  // Instantiate all pipeline stages and barriers
  // =========================================================================

  val ifStage    = Module(new IF(BinaryFile))
  val ifBarrier  = Module(new IFBarrier)

  val idStage    = Module(new ID)
  val idBarrier  = Module(new IDBarrier)

  val exStage    = Module(new EX)
  val exBarrier  = Module(new EXBarrier)

  val memStage   = Module(new MEM)
  val memBarrier = Module(new MEMBarrier)

  val wbStage    = Module(new WB)
  val wbBarrier  = Module(new WBBarrier)

  val regFileInst    = Module(new regFile)
  val forwardingUnit = Module(new ForwardingUnit)

  // =========================================================================
  // Connect pipeline stages
  // =========================================================================

  // IF → IF Barrier
  ifBarrier.io.inInstr := ifStage.io.instr

  // IF Barrier → ID Stage
  idStage.io.instr := ifBarrier.io.outInstr

  // ID Stage ↔ Register File (read ports)
  regFileInst.io.req_1     := idStage.io.regFileReq_A
  idStage.io.regFileResp_A := regFileInst.io.resp_1
  regFileInst.io.req_2     := idStage.io.regFileReq_B
  idStage.io.regFileResp_B := regFileInst.io.resp_2

  // ID Stage → ID Barrier
  idBarrier.io.inUOP         := idStage.io.uop
  idBarrier.io.inRD          := idStage.io.rd
  idBarrier.io.inRS1         := idStage.io.rs1
  idBarrier.io.inRS2         := idStage.io.rs2
  idBarrier.io.inIsRType     := idStage.io.isRType
  idBarrier.io.inOperandA    := idStage.io.operandA
  idBarrier.io.inOperandB    := idStage.io.operandB
  idBarrier.io.inXcptInvalid := idStage.io.XcptInvalid

  // Forwarding Unit: detects RAW hazards for the instruction currently in EX
  forwardingUnit.io.rs1   := idBarrier.io.outRS1
  forwardingUnit.io.rs2   := idBarrier.io.outRS2
  forwardingUnit.io.exRD  := exBarrier.io.outRD
  forwardingUnit.io.memRD := memBarrier.io.outRD

  // ID Barrier → EX Stage (with forwarding signals)
  exStage.io.uop             := idBarrier.io.outUOP
  exStage.io.operandA        := idBarrier.io.outOperandA
  exStage.io.operandB        := idBarrier.io.outOperandB
  exStage.io.isRType         := idBarrier.io.outIsRType
  exStage.io.XcptInvalid     := idBarrier.io.outXcptInvalid
  exStage.io.forwardA        := forwardingUnit.io.forwardA
  exStage.io.forwardB        := forwardingUnit.io.forwardB
  exStage.io.forwardedExData  := exBarrier.io.outAluResult
  exStage.io.forwardedMemData := memBarrier.io.outAluResult

  // EX Stage → EX Barrier (RD passed from ID barrier, bypassing EX)
  exBarrier.io.inAluResult   := exStage.io.aluResult
  exBarrier.io.inRD          := idBarrier.io.outRD
  exBarrier.io.inXcptInvalid := exStage.io.exception

  // EX Barrier → MEM Barrier (MEM stage is a placeholder with no I/O)
  memBarrier.io.inAluResult := exBarrier.io.outAluResult
  memBarrier.io.inRD        := exBarrier.io.outRD
  memBarrier.io.inException := exBarrier.io.outXcptInvalid

  // MEM Barrier → WB Stage
  wbStage.io.aluResult := memBarrier.io.outAluResult
  wbStage.io.rd        := memBarrier.io.outRD

  // WB Stage ↔ Register File (write port)
  regFileInst.io.req_3 := wbStage.io.regFileReq

  // WB Stage → WB Barrier
  wbBarrier.io.inCheckRes    := wbStage.io.check_res
  wbBarrier.io.inXcptInvalid := memBarrier.io.outException

  // WB Barrier → Outputs
  io.check_res := wbBarrier.io.outCheckRes
  io.exception := wbBarrier.io.outXcptInvalid

}
