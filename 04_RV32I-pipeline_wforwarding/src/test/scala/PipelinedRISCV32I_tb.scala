// ADS I Class Project
// Pipelined RISC-V Core
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/15/2023 by Tobias Jauch (@tojauch)

package PipelinedRV32I_Tester

import chisel3._
import chiseltest._
import PipelinedRV32I._
import org.scalatest.flatspec.AnyFlatSpec

class PipelinedRISCV32ITest extends AnyFlatSpec with ChiselScalatestTester {

"RV32I_BasicTester" should "work" in {
    test(new PipelinedRV32I("src/test/programs/BinaryFile_pipelined")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      dut.clock.setTimeout(0)
      
      println("=== SECTION 1: Basic Operations ===")

      dut.clock.step(5)  // Wait for pipeline to fill (5 stages)
      dut.io.result.expect(0.U)     // ADDI x0, x0, 0
      dut.io.exception.expect(false.B)
      println("PASS: NOP -> 0")

      dut.clock.step(1)
      dut.io.result.expect(4.U)     // ADDI x1, x0, 4
      dut.io.exception.expect(false.B)
      println("PASS: ADDI x1, x0, 4 -> 4")

      dut.clock.step(1)
      dut.io.result.expect(5.U)     // ADDI x2, x0, 5
      dut.io.exception.expect(false.B)
      println("PASS: ADDI x2, x0, 5 -> 5")

    //  dut.clock.step(1)
    //  dut.io.result.expect(0.U)     // NOP
    //  dut.io.exception.expect(false.B)
    //  dut.clock.step(1)
    //  dut.io.result.expect(0.U)     // NOP
    //  dut.io.exception.expect(false.B)
    //  dut.clock.step(1)
    //  dut.io.result.expect(0.U)     // NOP
    //  dut.io.exception.expect(false.B)

      dut.clock.step(1)
      dut.io.result.expect(9.U)     // ADD x3, x1, x2
      dut.io.exception.expect(false.B)
      println("PASS: ADD x3, x1, x2 -> 9")

      // NOPs
      dut.clock.step(1)
      dut.io.result.expect(0.U)
      dut.clock.step(1)
      dut.io.result.expect(0.U)
      dut.clock.step(1)
      dut.io.result.expect(0.U)



      println("\n=== ALL TESTS PASSED ===")
    }
  }
}
