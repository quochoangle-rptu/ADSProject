// ADS I Class Project
// Pipelined RISC-V Core - Common Definitions
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
Global Definitions and Data Types

Enumerations:
    uopc: ChiselEnum defining micro-operation codes for all supported RV32I instructions:
        R-type instructions 
        I-type instructions
        NOP (no operation, default case)

This enum is used throughout the pipeline:
    Decode stage assigns uop based on instruction fields
    Execute stage maps uop to ALU operations
*/

package core_tile

import chisel3._
import chisel3.experimental.ChiselEnum

object uopc extends ChiselEnum {
  val NOP = Value
  val ADD = Value
  val SUB = Value
  val AND = Value
  val OR  = Value
  val XOR = Value
  val SLL = Value
  val SRL = Value
  val SRA = Value
  val SLT = Value
  val SLTU = Value
}
