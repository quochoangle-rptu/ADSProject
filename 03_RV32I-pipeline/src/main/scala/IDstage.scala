// ADS I Class Project
// Pipelined RISC-V Core - ID Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
Instruction Decode (ID) Stage: decoding and operand fetch

Extracted Fields from 32-bit Instruction (see RISC-V specification for reference):
    opcode: instruction format identifier
    funct3: selects variant within instruction format
    funct7: further specifies operation type (R-type only)
    rd: destination register address
    rs1: first source register address
    rs2: second source register address
    imm: 12-bit immediate value (I-type, sign-extended)

Register File Interfaces:
    regFileReq_A, regFileResp_A: read port for rs1 operand
    regFileReq_B, regFileResp_B: read port for rs2 operand

Internal Signals:
    Combinational decoders for instructions

Functionality:
    Decode opcode to determine instruction and identify operation (ADD, SUB, XOR, ...)
    Output: uop (operation code), rd, operandA (from rs1), operandB (rs2 or immediate)

Outputs:
    uop: micro-operation code (identifies instruction type)
    rd: destination register index
    operandA: first operand
    operandB: second operand 
    XcptInvalid: exception flag for invalid instructions
*/

package core_tile

import chisel3._
import chisel3.util._
//import uopc._
import core_tile.uopc._ //bug since uopc is an object inside core_tile package

// -----------------------------------------
// Decode Stage
// -----------------------------------------

class ID extends Module {
  val io = IO(new Bundle {

    // Input from IF barrier
    val instr = Input(UInt(32.W))

    // Writeback interface from WB stage
    val wbRegWrite = Input(Bool())
    val wbRd       = Input(UInt(5.W))
    val wbData     = Input(UInt(32.W))

    // Outputs to ID barrier
    val opA      = Output(UInt(32.W))
    val opB      = Output(UInt(32.W))
    val imm      = Output(UInt(32.W))
    //val aluOp    = Output(UInt(UOP_WIDTH.W))
    val aluOp    = Output(uopc())
    val rd       = Output(UInt(5.W))
    val regWrite = Output(Bool())
    val exception = Output(Bool())
  })

  // ------------------------------------------------------------
  // Instruction fields
  // ------------------------------------------------------------
  val opcode = io.instr(6, 0)
  val rd     = io.instr(11, 7)
  val funct3 = io.instr(14, 12)
  val rs1    = io.instr(19, 15)
  val rs2    = io.instr(24, 20)
  val funct7 = io.instr(31, 25)

  // ------------------------------------------------------------
  // Register File
  // ------------------------------------------------------------
  val regFile = Module(new regFile)

  // Read ports
  regFile.io.req_1.addr := rs1
  regFile.io.req_2.addr := rs2

  // Writeback
  regFile.io.req_3.wr_en := io.wbRegWrite
  regFile.io.req_3.addr  := io.wbRd
  regFile.io.req_3.data  := io.wbData

  // ------------------------------------------------------------
  // Immediate generation (I-type)
  // ------------------------------------------------------------
  val immI = Cat(Fill(20, io.instr(31)), io.instr(31, 20))

  // ------------------------------------------------------------
  // Defaults
  // ------------------------------------------------------------
  io.opA       := regFile.io.resp_1.data
  io.opB       := regFile.io.resp_2.data
  io.imm       := immI
  io.rd        := rd
  io.regWrite  := false.B
  io.aluOp     := uopc.NOP
  io.exception := false.B

  // ------------------------------------------------------------
  // Decode logic
  // ------------------------------------------------------------
  switch(opcode) {

    // -------------------------
    // R-type instructions
    // -------------------------
    is("b0110011".U) {
      io.regWrite := true.B
      io.opB      := regFile.io.resp_2.data

      switch(Cat(funct7, funct3)) {
        is("b0000000000".U) { io.aluOp := uopc.ADD }
        is("b0100000000".U) { io.aluOp := uopc.SUB }
        is("b0000000100".U) { io.aluOp := uopc.XOR }
        is("b0000000110".U) { io.aluOp := uopc.OR  }
        is("b0000000111".U) { io.aluOp := uopc.AND }
        is("b0000000001".U) { io.aluOp := uopc.SLL }
        is("b0000000101".U) { io.aluOp := uopc.SRL }
        is("b0100000101".U) { io.aluOp := uopc.SRA }
        is("b0000000010".U) { io.aluOp := uopc.SLT }
        is("b0000000011".U) { io.aluOp := uopc.SLTU }
      }
    }

    // -------------------------
    // I-type ALU instructions
    // -------------------------
    is("b0010011".U) {
      io.regWrite := true.B
      io.opB      := immI

      switch(funct3) {
        is("b000".U) { io.aluOp := uopc.ADD }   // ADDI
        is("b100".U) { io.aluOp := uopc.XOR }   // XORI
        is("b110".U) { io.aluOp := uopc.OR  }   // ORI
        is("b111".U) { io.aluOp := uopc.AND }   // ANDI
        is("b010".U) { io.aluOp := uopc.SLT }   // SLTI
        is("b011".U) { io.aluOp := uopc.SLTU }  // SLTIU
        is("b001".U) { io.aluOp := uopc.SLL }   // SLLI
        is("b101".U) {
          when(funct7 === "b0000000".U) { io.aluOp := uopc.SRL }
          .elsewhen(funct7 === "b0100000".U) { io.aluOp := uopc.SRA }
          .otherwise { io.exception := true.B }
        }
      }
    }
  }
}
