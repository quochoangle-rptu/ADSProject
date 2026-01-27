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
import chisel3.util.experimental.loadMemoryFromFile

class PipelinedRV32Icore(BinaryFile: String) extends Module {

  val io = IO(new Bundle {
    val result    = Output(UInt(32.W))
    val exception = Output(Bool())
  })

  // ------------------------------------------------------------
  // Stage Instances
  // ------------------------------------------------------------
  val ifStage  = Module(new IF(BinaryFile))
  val idStage  = Module(new ID)
  val exStage  = Module(new EX)
  val memStage = Module(new MEM)
  val wbStage  = Module(new WB)

  // ------------------------------------------------------------
  // Pipeline Barriers
  // ------------------------------------------------------------
  val ifBarrier  = Module(new IFBarrier)
  val idBarrier  = Module(new IDBarrier)
  val exBarrier  = Module(new EXBarrier)
  val memBarrier = Module(new MEMBarrier)
  
  
  // ------------------------------------------------------------
  // Pipeline valid shift register (5-stage pipeline)
  // ------------------------------------------------------------
  //val validPipe = RegInit(VecInit(Seq.fill(5)(false.B)))

  //validPipe(0) := true.B
  //for (i <- 1 until 5) {
  //  validPipe(i) := validPipe(i-1)
  //}

  //val wbValid = validPipe(4)
  
  // ***************************************************
  // outputs of one stage are input to the next stages, outputs are driven to inputs within the individual modules.

  // ------------------------------------------------------------
  // IF Stage
  // ------------------------------------------------------------
  //ifStage.io.instr := imem(ifStage.io.pc >> 2.U)

  // IF → IFBarrier
  ifBarrier.io.inInstr := ifStage.io.instr

  // ------------------------------------------------------------
  // ID Stage
  // ------------------------------------------------------------
  idStage.io.instr := ifBarrier.io.outInstr
  idStage.io.wbRegWrite := wbStage.io.regWrite
  idStage.io.wbRd       := wbStage.io.rd
  idStage.io.wbData     := wbStage.io.wbData


  // ------------------------------------------------------------
  // ID → IDBarrier
  // ------------------------------------------------------------
  idBarrier.io.inUOP         := idStage.io.aluOp
  idBarrier.io.inRD          := idStage.io.rd
  idBarrier.io.inOperandA    := idStage.io.opA
  idBarrier.io.inOperandB    := idStage.io.opB
  idBarrier.io.inXcptInvalid := idStage.io.exception
  idBarrier.io.inRegWrite    := idStage.io.regWrite


  // ------------------------------------------------------------
  // EX Stage
  // ------------------------------------------------------------
  exStage.io.uop         := idBarrier.io.outUOP
  exStage.io.opA         := idBarrier.io.outOperandA
  exStage.io.opB         := idBarrier.io.outOperandB
  exStage.io.rd          := idBarrier.io.outRD
  exStage.io.regWriteIn  := idBarrier.io.outRegWrite
  exStage.io.exceptionIn := idBarrier.io.outXcptInvalid

  // ------------------------------------------------------------
  // EX → EXBarrier
  // ------------------------------------------------------------
  exBarrier.io.inAluResult   := exStage.io.aluRes
  exBarrier.io.inRD          := exStage.io.rdOut
  exBarrier.io.inRegWrite    := exStage.io.regWrite
  exBarrier.io.inXcptInvalid := exStage.io.exception

  // ------------------------------------------------------------
  // EXBarrier → MEM
  // ------------------------------------------------------------
  memStage.io.aluRes    := exBarrier.io.outAluResult
  memStage.io.rd        := exBarrier.io.outRD
  memStage.io.regWrite  := exBarrier.io.outRegWrite
  memStage.io.exception := exBarrier.io.outXcptInvalid


  // ------------------------------------------------------------
  // MEM → MEMBarrier regWrite is not propogated in this stage rather directly from EX to WB since this stage does nothing
  // ------------------------------------------------------------  
  memBarrier.io.inAluResult := memStage.io.aluResOut
  memBarrier.io.inRD        := memStage.io.rdOut
  memBarrier.io.inException := memStage.io.exceptionOut
  memBarrier.io.inRegWrite  := memStage.io.regWriteOut

  // ------------------------------------------------------------
  // MemBarrier --> WB Stage
  // ------------------------------------------------------------
  wbStage.io.aluRes    := memBarrier.io.outAluResult
  wbStage.io.rd        := memBarrier.io.outRD
  wbStage.io.regWrite  := memBarrier.io.outRegWrite
  wbStage.io.exception := memBarrier.io.outException


  // ------------------------------------------------------------
  // Outputs to Testbench
  // ------------------------------------------------------------
  io.result    := wbStage.io.wbData
  //io.result := Mux(wbValid, wbStage.io.wbData, 0.U)
  io.exception := wbStage.io.exception
}

