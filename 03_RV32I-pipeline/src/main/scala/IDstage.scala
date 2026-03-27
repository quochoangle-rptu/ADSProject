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
import uopc._

// -----------------------------------------
// Decode Stage
// -----------------------------------------

class ID extends Module {
  val io = IO(new Bundle {
    // Input from IF barrier
    val instr = Input(UInt(32.W))

    // Register file read interface
    val regFileReq_A  = Output(new regFileReadReq)
    val regFileResp_A = Input(new regFileReadResp)
    val regFileReq_B  = Output(new regFileReadReq)
    val regFileResp_B = Input(new regFileReadResp)

    // Outputs to ID barrier
    val uop         = Output(uopc())
    val rd          = Output(UInt(5.W))
    val rs1         = Output(UInt(5.W))   // source register 1 address (for forwarding unit)
    val rs2         = Output(UInt(5.W))   // source register 2 address (for forwarding unit)
    val isRType     = Output(Bool())       // true when operandB comes from rs2 (R-type)
    val operandA    = Output(UInt(32.W))
    val operandB    = Output(UInt(32.W))
    val XcptInvalid = Output(Bool())
  })

  // Extract instruction fields
  val opcode = io.instr(6, 0)
  val rd     = io.instr(11, 7)
  val funct3 = io.instr(14, 12)
  val rs1    = io.instr(19, 15)
  val rs2    = io.instr(24, 20)
  val funct7 = io.instr(31, 25)

  // Sign-extended 12-bit immediate (I-type)
  val immI = io.instr(31, 20).asSInt.pad(32).asUInt

  // Issue register-file read requests
  io.regFileReq_A.addr := rs1
  io.regFileReq_B.addr := rs2

  // Default outputs
  io.uop         := NOP
  io.XcptInvalid := false.B
  io.rd          := rd
  io.rs1         := rs1
  io.rs2         := rs2
  io.isRType     := (opcode === Opcodes.R_TYPE)
  io.operandA    := io.regFileResp_A.data
  io.operandB    := io.regFileResp_B.data  // default: rs2 (R-type)

  when(opcode === Opcodes.R_TYPE) {
    switch(funct3) {
      is(Funct3.ADD_SUB) {
        when(funct7 === Funct7.NORMAL)     { io.uop := ADD }
        .elsewhen(funct7 === Funct7.ALT)   { io.uop := SUB }
        .otherwise                          { io.uop := NOP; io.XcptInvalid := true.B }
      }
      is(Funct3.SLL)  { io.uop := SLL }
      is(Funct3.SLT)  { io.uop := SLT }
      is(Funct3.SLTU) { io.uop := SLTU }
      is(Funct3.XOR)  { io.uop := XOR }
      is(Funct3.SRL_SRA) {
        when(funct7 === Funct7.NORMAL)     { io.uop := SRL }
        .elsewhen(funct7 === Funct7.ALT)   { io.uop := SRA }
        .otherwise                          { io.uop := NOP; io.XcptInvalid := true.B }
      }
      is(Funct3.OR)  { io.uop := OR }
      is(Funct3.AND) { io.uop := AND }
    }
  }.elsewhen(opcode === Opcodes.I_TYPE) {
    io.operandB := immI  // use sign-extended immediate
    switch(funct3) {
      is(Funct3.ADD_SUB) { io.uop := ADDI }
      is(Funct3.SLT)     { io.uop := SLTI }
      is(Funct3.SLTU)    { io.uop := SLTIU }
      is(Funct3.XOR)     { io.uop := XORI }
      is(Funct3.OR)      { io.uop := ORI }
      is(Funct3.AND)     { io.uop := ANDI }
      is(Funct3.SLL) {
        when(funct7 === Funct7.NORMAL) {
          io.uop      := SLLI
          io.operandB := rs2.pad(32)  // shamt is bits [24:20]
        }.otherwise { io.uop := NOP; io.XcptInvalid := true.B }
      }
      is(Funct3.SRL_SRA) {
        when(funct7 === Funct7.NORMAL)   {
          io.uop      := SRLI
          io.operandB := rs2.pad(32)
        }.elsewhen(funct7 === Funct7.ALT) {
          io.uop      := SRAI
          io.operandB := rs2.pad(32)
        }.otherwise { io.uop := NOP; io.XcptInvalid := true.B }
      }
    }
  }.otherwise {
    io.uop         := NOP
    io.XcptInvalid := true.B
  }
} 