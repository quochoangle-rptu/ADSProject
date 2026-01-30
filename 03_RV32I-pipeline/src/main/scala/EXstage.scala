// ADS I Class Project
// Pipelined RISC-V Core - EX Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
Instruction Execute (EX) Stage: ALU operations and exception detection

Instantiated Modules:
    ALU: Integrate your module from Assignment02 for arithmetic/logical operations

ALU Interface:
    alu.io.operandA: first operand input
    alu.io.operandB: second operand input
    alu.io.operation: operation code controlling ALU function
    alu.io.aluResult: computation result output

Internal Signals:
    Map uopc codes to ALUOp values

Functionality:
    Map instruction uop to ALU operation code
    Pass operands to ALU
    Output results to pipeline

Outputs:
    aluResult: computation result from ALU
    exception: pass exception flag
*/

package core_tile

import chisel3._
import chisel3.util._
import Assignment02.{ALU, ALUOp}
import uopc._

// -----------------------------------------
// Execute Stage
// -----------------------------------------
class EX extends Module {
  val io = IO(new Bundle {
    // Inputs from ID barrier
    val uop         = Input(uopc())
    val operandA    = Input(UInt(32.W))
    val operandB    = Input(UInt(32.W))
    val XcptInvalid = Input(Bool())

    // Outputs to EX barrier
    val aluResult   = Output(UInt(32.W))
    val exception   = Output(Bool())
  })

  // Instantiate ALU from Assignment02
  val alu = Module(new ALU)

  // Connect operands to ALU
  alu.io.operandA := io.operandA
  alu.io.operandB := io.operandB

  // Default ALU operation
  alu.io.operation := ALUOp.ADD

  // Map micro-operation codes to ALU operations
  // R-type and I-type instructions map to the same ALU operations
  switch(io.uop) {
    // Addition operations
    is(ADD)  { alu.io.operation := ALUOp.ADD }
    is(ADDI) { alu.io.operation := ALUOp.ADD }

    // Subtraction (R-type only, no SUBI in RISC-V)
    is(SUB)  { alu.io.operation := ALUOp.SUB }

    // Bitwise AND
    is(AND)  { alu.io.operation := ALUOp.AND }
    is(ANDI) { alu.io.operation := ALUOp.AND }

    // Bitwise OR
    is(OR)   { alu.io.operation := ALUOp.OR }
    is(ORI)  { alu.io.operation := ALUOp.OR }

    // Bitwise XOR
    is(XOR)  { alu.io.operation := ALUOp.XOR }
    is(XORI) { alu.io.operation := ALUOp.XOR }

    // Shift Left Logical
    is(SLL)  { alu.io.operation := ALUOp.SLL }
    is(SLLI) { alu.io.operation := ALUOp.SLL }

    // Shift Right Logical
    is(SRL)  { alu.io.operation := ALUOp.SRL }
    is(SRLI) { alu.io.operation := ALUOp.SRL }

    // Shift Right Arithmetic
    is(SRA)  { alu.io.operation := ALUOp.SRA }
    is(SRAI) { alu.io.operation := ALUOp.SRA }

    // Set Less Than (signed)
    is(SLT)  { alu.io.operation := ALUOp.SLT }
    is(SLTI) { alu.io.operation := ALUOp.SLT }

    // Set Less Than Unsigned
    is(SLTU)  { alu.io.operation := ALUOp.SLTU }
    is(SLTIU) { alu.io.operation := ALUOp.SLTU }

    // NOP: default ADD with operands = 0 produces 0
    is(NOP)  { alu.io.operation := ALUOp.ADD }
  }

  // Output ALU result
  io.aluResult := alu.io.aluResult

  // Pass through exception flag
  io.exception := io.XcptInvalid
}
//ToDo: Add your implementation according to the specification above here 
