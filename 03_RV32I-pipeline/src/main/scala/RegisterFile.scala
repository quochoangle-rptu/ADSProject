// ADS I Class Project
// Pipelined RISC-V Core - Register File
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

package core_tile

import chisel3._
import chisel3.util._

// -----------------------------------------
// Register File Bundles
// -----------------------------------------

class regFileReadReq extends Bundle {
  val addr = UInt(5.W)
}

class regFileReadResp extends Bundle {
  val data = UInt(32.W)
}

class regFileWriteReq extends Bundle {
  val addr  = UInt(5.W)
  val data  = UInt(32.W)
  val wr_en = Bool()
}

// -----------------------------------------
// Register File
// -----------------------------------------

class regFile extends Module {
  val io = IO(new Bundle {
    // Read port 1
    val req_1  = Input(new regFileReadReq)
    val resp_1 = Output(new regFileReadResp)

    // Read port 2
    val req_2  = Input(new regFileReadReq)
    val resp_2 = Output(new regFileReadResp)

    // Write port
    val req_3  = Input(new regFileWriteReq)
  })

  // 32 registers of 32 bits each
  val regs = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

  // -------------------------
  // Read logic (combinational)
  // -------------------------

  io.resp_1.data := Mux(
    io.req_1.addr === 0.U,
    0.U,
    regs(io.req_1.addr)
  )

  io.resp_2.data := Mux(
    io.req_2.addr === 0.U,
    0.U,
    regs(io.req_2.addr)
  )

  // -------------------------
  // Write logic (synchronous)
  // -------------------------

  when(io.req_3.wr_en && (io.req_3.addr =/= 0.U)) {
    regs(io.req_3.addr) := io.req_3.data
  }
}
