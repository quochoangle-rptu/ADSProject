// ADS I Class Project
// Assignment 02: Arithmetic Logic Unit and UVM Testbench
//
// Chair of Electronic Design Automation, RPTU University Kaiserslautern-Landau
// File created on 09/21/2025 by Tharindu Samarakoon (gug75kex@rptu.de)
// File updated on 10/29/2025 by Tobias Jauch (tobias.jauch@rptu.de)

package Assignment02

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

// =============================================================================
// ALU Operations Enum
// =============================================================================
// ChiselEnum creates a type-safe enumeration that synthesizes to hardware.
// Values are automatically assigned: ADD=0, SUB=1, AND=2, etc.
// IMPORTANT: This order must match the SystemVerilog enum in alu_tb_config_pkg.sv!

object ALUOp extends ChiselEnum {
  val ADD   = Value(0.U(8.W))   // 8'h0
  val SUB   = Value(1.U(8.W))   // 8'h1
  val AND   = Value(2.U(8.W))   // 8'h2
  val OR    = Value(3.U(8.W))   // 8'h3
  val XOR   = Value(4.U(8.W))   // 8'h4
  val SLL   = Value(5.U(8.W))   // 8'h5
  val SRL   = Value(6.U(8.W))   // 8'h6
  val SRA   = Value(7.U(8.W))   // 8'h7
  val SLT   = Value(8.U(8.W))   // 8'h8
  val SLTU  = Value(9.U(8.W))   // 8'h9
  val PASSB = Value(10.U(8.W))  // 8'hA — pass operandB (used for LUI)
}

// =============================================================================
// ALU Module
// =============================================================================
// A purely combinational 32-bit ALU for the RV32I RISC-V ISA.
// No internal state - output depends only on current inputs.

class ALU extends Module {

  val io = IO(new Bundle {
    val operandA  = Input(UInt(32.W))   // First operand (32-bit unsigned)
    val operandB  = Input(UInt(32.W))   // Second operand (32-bit unsigned)
    val operation = Input(ALUOp())      // Operation selector (enum type)
    val aluResult = Output(UInt(32.W))  // Result output (32-bit unsigned)
  })

  // For RV32I shift operations only the lower 5 bits of operandB are used.
  val shamt = io.operandB(4, 0)

  // Default output prevents latch inference.
  io.aluResult := 0.U

  switch(io.operation) {

    is(ALUOp.ADD) {
      io.aluResult := io.operandA + io.operandB
    }

    is(ALUOp.SUB) {
      io.aluResult := io.operandA - io.operandB
    }

    is(ALUOp.AND) {
      io.aluResult := io.operandA & io.operandB
    }

    is(ALUOp.OR) {
      io.aluResult := io.operandA | io.operandB
    }

    is(ALUOp.XOR) {
      io.aluResult := io.operandA ^ io.operandB
    }

    // SLL: shift left logical — zeros fill from the right.
    // Truncate to 32 bits to avoid width growth.
    is(ALUOp.SLL) {
      io.aluResult := (io.operandA << shamt)(31, 0)
    }

    // SRL: shift right logical — zeros fill from the left.
    is(ALUOp.SRL) {
      io.aluResult := io.operandA >> shamt
    }

    // SRA: shift right arithmetic — sign bit is replicated.
    is(ALUOp.SRA) {
      io.aluResult := (io.operandA.asSInt >> shamt).asUInt
    }

    // SLT: set-less-than (signed).  Returns 1 if operandA < operandB.
    is(ALUOp.SLT) {
      io.aluResult := (io.operandA.asSInt < io.operandB.asSInt).asUInt
    }

    // SLTU: set-less-than unsigned.  Returns 1 if operandA < operandB.
    is(ALUOp.SLTU) {
      io.aluResult := (io.operandA < io.operandB).asUInt
    }

    // PASSB: forward operandB unchanged (used by LUI / AUIPC).
    is(ALUOp.PASSB) {
      io.aluResult := io.operandB
    }
  }
}
