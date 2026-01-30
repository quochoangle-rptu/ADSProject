// ADS I Class Project
// Pipelined RISC-V Core - IF Barrier
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
IF-Barrier: pipeline register between Fetch and Decode stages

Internal Registers:
    instrReg: holds instruction between pipeline stages, initialized to 0

Inputs:
    inInstr: fetched instruction from IF stage

Outputs:
    outInstr: instruction to ID stage

Functionality:
    Save all input signals to a register and output them in the following clock cycle
*/

package core_tile

import chisel3._

// -----------------------------------------
// IF-Barrier
// -----------------------------------------

class IFBarrier extends Module {
  val io = IO(new Bundle {
    //ToDo: Add I/O ports
    val inInstr  = Input(UInt(32.W))   // Instruction from IF stage
    val outInstr = Output(UInt(32.W))  // Instruction to ID stage
  })

//ToDo: Add your implementation according to the specification above here 
// Pipeline register: stores instruction for one clock cycle
// Initialized to NOP (ADDI x0, x0, 0 = 0x00000013), but 0 works too
  val instrReg = RegInit(0.U(32.W))

  // Capture input on clock edge
  instrReg := io.inInstr

  // Output registered value
  io.outInstr := instrReg
}
