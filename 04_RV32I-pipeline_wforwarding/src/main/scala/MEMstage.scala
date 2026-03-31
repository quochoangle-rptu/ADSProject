// ADS I Class Project
// Pipelined RISC-V Core - MEM Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
Memory (MEM) Stage: load/store operations (placeholder for RV32I R/I-type subset)

Current Implementation:
    Empty placeholder module with no active ports or operations
    In current RV32I subset (R-type, I-type), no memory operations are performed

Rationale:
    Placeholder stage ensures proper pipeline depth and timing
    Allows future extension without architectural changes
*/

package core_tile

import chisel3._

// -----------------------------------------
// Memory Stage
// -----------------------------------------

class MEM extends Module {
  val io = IO(new Bundle {

    // Inputs from EX barrier
    val aluRes    = Input(UInt(32.W))
    val rd        = Input(UInt(5.W))
    val regWrite  = Input(Bool())
    val exception = Input(Bool())

    // Outputs to MEM barrier
    val aluResOut    = Output(UInt(32.W))
    val rdOut        = Output(UInt(5.W))
    val regWriteOut  = Output(Bool())
    val exceptionOut = Output(Bool())
  })

  // ------------------------------------------------------------
  // Pass-through logic (no memory ops)
  // ------------------------------------------------------------
  io.aluResOut    := io.aluRes
  io.rdOut        := io.rd
  io.regWriteOut  := io.regWrite
  io.exceptionOut := io.exception
}

