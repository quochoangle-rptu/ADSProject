// ADS I Class Project
// Pipelined RISC-V Core - Common Definitions
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
Global Definitions and Data Types

Enumerations:
    uopc: ChiselEnum defining micro-operation codes for all supported RV32I instructions:
        R-type instructions 
        I-type instructions
        NOP (no operation, default case)

This enum is used throughout the pipeline:
    Decode stage assigns uop based on instruction fields
    Execute stage maps uop to ALU operations
*/

package core_tile

import chisel3._
import chisel3.experimental.ChiselEnum

// -----------------------------------------
// Global Definitions and Data Types
// -----------------------------------------

//ToDo: Add your implementation according to the specification above here
object uopc extends ChiselEnum {
  // R-type instructions (opcode = 0110011)
  val ADD   = Value   // add rd, rs1, rs2
  val SUB   = Value   // sub rd, rs1, rs2
  val SLL   = Value   // sll rd, rs1, rs2
  val SLT   = Value   // slt rd, rs1, rs2
  val SLTU  = Value   // sltu rd, rs1, rs2
  val XOR   = Value   // xor rd, rs1, rs2
  val SRL   = Value   // srl rd, rs1, rs2
  val SRA   = Value   // sra rd, rs1, rs2
  val OR    = Value   // or rd, rs1, rs2
  val AND   = Value   // and rd, rs1, rs2

  // I-type instructions (opcode = 0010011)
  val ADDI  = Value   // addi rd, rs1, imm
  val SLTI  = Value   // slti rd, rs1, imm
  val SLTIU = Value   // sltiu rd, rs1, imm
  val XORI  = Value   // xori rd, rs1, imm
  val ORI   = Value   // ori rd, rs1, imm
  val ANDI  = Value   // andi rd, rs1, imm
  val SLLI  = Value   // slli rd, rs1, shamt
  val SRLI  = Value   // srli rd, rs1, shamt
  val SRAI  = Value   // srai rd, rs1, shamt

  // NOP / Invalid instruction
  val NOP   = Value   // No operation (used for invalid instructions)
}

// RISC-V Opcodes (bits [6:0] of instruction)
object Opcodes {
  val R_TYPE = "b0110011".U(7.W)  // R-type instructions
  val I_TYPE = "b0010011".U(7.W)  // I-type ALU instructions
}

// Funct3 encodings for R-type and I-type instructions
object Funct3 {
  val ADD_SUB = "b000".U(3.W)  // ADD/SUB/ADDI
  val SLL     = "b001".U(3.W)  // SLL/SLLI
  val SLT     = "b010".U(3.W)  // SLT/SLTI
  val SLTU    = "b011".U(3.W)  // SLTU/SLTIU
  val XOR     = "b100".U(3.W)  // XOR/XORI
  val SRL_SRA = "b101".U(3.W)  // SRL/SRA/SRLI/SRAI
  val OR      = "b110".U(3.W)  // OR/ORI
  val AND     = "b111".U(3.W)  // AND/ANDI
}

// Funct7 encodings (bit 30 distinguishes some operations)
object Funct7 {
  val NORMAL  = "b0000000".U(7.W)  // ADD, SRL, etc.
  val ALT     = "b0100000".U(7.W)  // SUB, SRA
}

