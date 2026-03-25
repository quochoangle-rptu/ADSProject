package core_tile

import chisel3._
import chisel3.util._

// Value	Meaning
// 0.U	- use original operand (from ID/EX)
// 1.U	- forward from EX/MEM
// 2.U	- forward from MEM/WB

// rd =/= 0.U → avoid forwarding from x0
// EX/MEM has priority over MEM/WB → correct ordering

class ForwardingUnit extends Module {
  val io = IO(new Bundle {

    // From ID/EX stage (current instruction)
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))

    // From EX/MEM stage
    val ex_mem_rd       = Input(UInt(5.W))
    val ex_mem_regWrite = Input(Bool())

    // From MEM/WB stage
    val mem_wb_rd       = Input(UInt(5.W))
    val mem_wb_regWrite = Input(Bool())

    // Outputs: control signals for muxes in EX stage
    val forwardA = Output(UInt(2.W))
    val forwardB = Output(UInt(2.W))
  })

  // Default: no forwarding (use register file values)
  io.forwardA := 0.U
  io.forwardB := 0.U

  // -----------------------------
  // Forwarding for operand A (rs1)
  // -----------------------------
  when (
    io.ex_mem_regWrite &&
    (io.ex_mem_rd =/= 0.U) &&
    (io.ex_mem_rd === io.rs1)
  ) {
    io.forwardA := 1.U   // from EX/MEM
  } .elsewhen (
    io.mem_wb_regWrite &&
    (io.mem_wb_rd =/= 0.U) &&
    (io.mem_wb_rd === io.rs1)
  ) {
    io.forwardA := 2.U   // from MEM/WB
  }

  // -----------------------------
  // Forwarding for operand B (rs2)
  // -----------------------------
  when (
    io.ex_mem_regWrite &&
    (io.ex_mem_rd =/= 0.U) &&
    (io.ex_mem_rd === io.rs2)
  ) {
    io.forwardB := 1.U   // from EX/MEM
  } .elsewhen (
    io.mem_wb_regWrite &&
    (io.mem_wb_rd =/= 0.U) &&
    (io.mem_wb_rd === io.rs2)
  ) {
    io.forwardB := 2.U   // from MEM/WB
  }
    printf(p"""
    rs1=${io.rs1}
    EX: rd=${io.ex_mem_rd}, rw=${io.ex_mem_regWrite}
    WB: rd=${io.mem_wb_rd}, rw=${io.mem_wb_regWrite}
    forwardA=${io.forwardA}
    \n""")
}