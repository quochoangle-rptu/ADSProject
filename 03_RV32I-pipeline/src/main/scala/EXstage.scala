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
    val uop         = Input(UInt(UOP_WIDTH.W))
    val opA         = Input(UInt(32.W))
    val opB         = Input(UInt(32.W))
    val rd          = Input(UInt(5.W))
    val regWriteIn  = Input(Bool())
    val exceptionIn = Input(Bool())

    // Outputs to EX barrier
    val aluRes    = Output(UInt(32.W))
    val rdOut     = Output(UInt(5.W))
    val regWrite  = Output(Bool())
    val exception = Output(Bool())
  })

  // ------------------------------------------------------------
  // ALU instantiation
  // ------------------------------------------------------------
  val alu = Module(new ALU)

  alu.io.operandA := io.opA
  alu.io.operandB := io.opB

  // ------------------------------------------------------------
  // Default ALU operation
  // ------------------------------------------------------------
  alu.io.operation := ALUOp.ADD

  // ------------------------------------------------------------
  // uop → ALUOp mapping
  // ------------------------------------------------------------
  switch(io.uop) {
    is(UOP_ADD)  { alu.io.operation := ALUOp.ADD  }
    is(UOP_SUB)  { alu.io.operation := ALUOp.SUB  }
    is(UOP_AND)  { alu.io.operation := ALUOp.AND  }
    is(UOP_OR)   { alu.io.operation := ALUOp.OR   }
    is(UOP_XOR)  { alu.io.operation := ALUOp.XOR  }
    is(UOP_SLL)  { alu.io.operation := ALUOp.SLL  }
    is(UOP_SRL)  { alu.io.operation := ALUOp.SRL  }
    is(UOP_SRA)  { alu.io.operation := ALUOp.SRA  }
    is(UOP_SLT)  { alu.io.operation := ALUOp.SLT  }
    is(UOP_SLTU) { alu.io.operation := ALUOp.SLTU }
  }

  // ------------------------------------------------------------
  // Outputs
  // ------------------------------------------------------------
  io.aluRes    := alu.io.aluResult
  io.rdOut     := io.rd
  io.regWrite  := io.regWriteIn
  io.exception := io.exceptionIn //Exception is only propogated since ALU has no exception handling mechanism
}

