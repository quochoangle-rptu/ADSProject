// ADS I Class Project
// Pipelined RISC-V Core - Register File
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

package core_tile

import chisel3._

/*
Register File Module: 32x32-bit dual-read single-write register file

Memory:
    regFile: Register file according to the RISC-V 32I specification

Ports:
    req_1, resp_1: first read port
        req_1.addr: read address for register x[0-31]
        resp_1.data: register data output
    req_2, resp_2: second read port
        req_2.addr: read address for register x[0-31]
        resp_2.data: register data output
    req_3: write port
        req_3.addr: write destination address
        req_3.data: data to write
        req_3.wr_en: write enable signal

Functionality:
    Two read ports allow simultaneous reading of two operands
    Synchronous write updates register if wr_en is asserted
*/

// -----------------------------------------
// Register File
// -----------------------------------------

class regFileReadReq extends Bundle {
    //ToDo: implement bundle for read request
    val addr = Input(UInt(5.W))  // 5 bits to address 32 registers (x0-x31)
}

class regFileReadResp extends Bundle {
    //ToDo: implement bundle for read response
    val data = Output(UInt(32.W))  // 32-bit register data
}

class regFileWriteReq extends Bundle {
    //ToDo: implement bundle for write request
    val addr  = Input(UInt(5.W))   // Destination register address
    val data  = Input(UInt(32.W))  // Data to write
    val wr_en = Input(Bool())      // Write enable signal
}

class regFile extends Module {
  val io = IO(new Bundle {
    //ToDo: Add I/O ports
    // Read port 1 (for rs1)
    val req_1  = new regFileReadReq
    val resp_1 = new regFileReadResp

    // Read port 2 (for rs2)
    val req_2  = new regFileReadReq
    val resp_2 = new regFileReadResp

    // Write port (for rd)
    val req_3  = new regFileWriteReq
})

//ToDo: Add your implementation according to the specification above here 
// Create register file: 32 registers, each 32 bits wide
// Initialize all registers to 0
  val registers = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

  // Read port 1: Return 0 if reading x0, otherwise return register value
  // Reads are combinational (asynchronous)
  io.resp_1.data := Mux(io.req_1.addr === 0.U, 0.U, registers(io.req_1.addr))

  // Read port 2: Return 0 if reading x0, otherwise return register value
  io.resp_2.data := Mux(io.req_2.addr === 0.U, 0.U, registers(io.req_2.addr))

  // Write port: Write data if write enable is asserted AND not writing to x0
  // Writes are synchronous (occur on clock edge)
  when(io.req_3.wr_en && io.req_3.addr =/= 0.U) {
    registers(io.req_3.addr) := io.req_3.data
  }
}
