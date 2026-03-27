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
    val isRType     = Input(Bool())        // true = R-type; enables operandB forwarding
    val XcptInvalid = Input(Bool())

    // Forwarding control from ForwardingUnit
    // 0 = no forward, 1 = MEM/WB forward, 2 = EX/MEM forward
    val forwardA        = Input(UInt(2.W))
    val forwardB        = Input(UInt(2.W))
    val forwardedExData  = Input(UInt(32.W)) // EX/MEM forwarded value (EXBarrier result)
    val forwardedMemData = Input(UInt(32.W)) // MEM/WB forwarded value (MEMBarrier result)

    // Outputs to EX barrier
    val aluResult   = Output(UInt(32.W))
    val exception   = Output(Bool())
  })

  // Instantiate ALU from Assignment02
  val alu = Module(new ALU)

  // ---- Forwarding muxes (placed before ALU inputs) ----------------------
  // operandA selects between: ID/EX value, MEM/WB forward, or EX/MEM forward
  val aluInputA = MuxCase(io.operandA, Seq(
    (io.forwardA === 2.U) -> io.forwardedExData,
    (io.forwardA === 1.U) -> io.forwardedMemData
  ))

  // operandB: only apply forwarding when the instruction is R-type
  // (I-type uses an immediate in operandB which must NOT be overwritten)
  val aluInputB = Mux(io.isRType,
    MuxCase(io.operandB, Seq(
      (io.forwardB === 2.U) -> io.forwardedExData,
      (io.forwardB === 1.U) -> io.forwardedMemData
    )),
    io.operandB  // I-type: operandB is already the correct sign-extended immediate
  )

  alu.io.operandA  := aluInputA
  alu.io.operandB  := aluInputB
  alu.io.operation := ALUOp.ADD  // default

  // Map micro-operation codes to ALU operations
  switch(io.uop) {
    is(ADD)   { alu.io.operation := ALUOp.ADD  }
    is(ADDI)  { alu.io.operation := ALUOp.ADD  }
    is(SUB)   { alu.io.operation := ALUOp.SUB  }
    is(AND)   { alu.io.operation := ALUOp.AND  }
    is(ANDI)  { alu.io.operation := ALUOp.AND  }
    is(OR)    { alu.io.operation := ALUOp.OR   }
    is(ORI)   { alu.io.operation := ALUOp.OR   }
    is(XOR)   { alu.io.operation := ALUOp.XOR  }
    is(XORI)  { alu.io.operation := ALUOp.XOR  }
    is(SLL)   { alu.io.operation := ALUOp.SLL  }
    is(SLLI)  { alu.io.operation := ALUOp.SLL  }
    is(SRL)   { alu.io.operation := ALUOp.SRL  }
    is(SRLI)  { alu.io.operation := ALUOp.SRL  }
    is(SRA)   { alu.io.operation := ALUOp.SRA  }
    is(SRAI)  { alu.io.operation := ALUOp.SRA  }
    is(SLT)   { alu.io.operation := ALUOp.SLT  }
    is(SLTI)  { alu.io.operation := ALUOp.SLT  }
    is(SLTU)  { alu.io.operation := ALUOp.SLTU }
    is(SLTIU) { alu.io.operation := ALUOp.SLTU }
    is(NOP)   { alu.io.operation := ALUOp.ADD  }
  }

  io.aluResult := alu.io.aluResult
  io.exception := io.XcptInvalid   // pass exception flag through
}