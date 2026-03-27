// ADS I Class Project
// Pipelined RISC-V Core - Forwarding Unit
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
Forwarding Unit: detects and resolves RAW data hazards by forwarding
results from later pipeline stages back to the ALU inputs in the EX stage.

Design follows the lecture schematic (slide 6-26):
  - EX/MEM hazard: instruction immediately following the producer (0-NOP gap)
    → forward from EX/MEM barrier (EXBarrier.outAluResult)
  - MEM/WB hazard: instruction two positions after the producer (1-NOP gap)
    → forward from MEM/WB barrier (MEMBarrier.outAluResult)

Hazard detection conditions (rd ≠ 0 guards against forwarding x0 writes):
  forwardA = 2 (EX/MEM)  if exRD  ≠ 0  AND exRD  = rs1
  forwardA = 1 (MEM/WB)  if memRD ≠ 0  AND memRD = rs1   (only when no EX hazard)
  forwardB = 2 (EX/MEM)  if exRD  ≠ 0  AND exRD  = rs2
  forwardB = 1 (MEM/WB)  if memRD ≠ 0  AND memRD = rs2   (only when no EX hazard)

forwardB is only meaningful for R-type instructions (the EX stage ignores it for
I-type instructions where operandB is an immediate, controlled by the isRType flag).

Forward select encoding:
  0 = no forwarding (use operand from ID/EX barrier)
  1 = forward from MEM/WB barrier (MEMBarrier output)
  2 = forward from EX/MEM barrier (EXBarrier output)

Inputs connected in core.scala:
  rs1, rs2   ← idBarrier.outRS1 / outRS2
  exRD       ← exBarrier.outRD
  memRD      ← memBarrier.outRD
*/

package core_tile

import chisel3._
import chisel3.util._

// -----------------------------------------
// Forwarding Unit
// -----------------------------------------

class ForwardingUnit extends Module {
  val io = IO(new Bundle {
    // RS1 and RS2 addresses of the instruction currently in the EX stage
    // (sourced from the ID/EX pipeline register)
    val rs1   = Input(UInt(5.W))
    val rs2   = Input(UInt(5.W))

    // RD of the instruction in the EX/MEM stage (EX barrier output)
    val exRD  = Input(UInt(5.W))

    // RD of the instruction in the MEM/WB stage (MEM barrier output)
    val memRD = Input(UInt(5.W))

    // Forward select signals for operand A (rs1) and operand B (rs2)
    val forwardA = Output(UInt(2.W))
    val forwardB = Output(UInt(2.W))
  })

  // ---- Forward A (operand A = rs1) ------------------------------------
  // EX/MEM hazard has priority over MEM/WB hazard (most recent result wins)
  io.forwardA := 0.U
  when(io.exRD =/= 0.U && io.exRD === io.rs1) {
    io.forwardA := 2.U            // EX/MEM hazard: forward from EX barrier
  }.elsewhen(io.memRD =/= 0.U && io.memRD === io.rs1) {
    io.forwardA := 1.U            // MEM/WB hazard: forward from MEM barrier
  }

  // ---- Forward B (operand B = rs2, R-type only) -----------------------
  io.forwardB := 0.U
  when(io.exRD =/= 0.U && io.exRD === io.rs2) {
    io.forwardB := 2.U            // EX/MEM hazard
  }.elsewhen(io.memRD =/= 0.U && io.memRD === io.rs2) {
    io.forwardB := 1.U            // MEM/WB hazard
  }
}
