// Copyright (c) 2018 Xilinx
//
// BSD v3 License
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice, this
//   list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above copyright notice,
//   this list of conditions and the following disclaimer in the documentation
//   and/or other materials provided with the distribution.
//
// * Neither the name of [project] nor the names of its
//   contributors may be used to endorse or promote products derived from
//   this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package bismo

import Chisel._
import fpgatidbits.streams._
import fpgatidbits.hlstools.TemplatedHLSBlackBox

// TODO: the params in SlidingWindowUnitHeader should really be template params
class SlidingWindowUnit extends TemplatedHLSBlackBox {
  val io = new Bundle {
    val strm_in = Decoupled(UInt(width = BISMOLimits.swuDescrInBits)).flip
    val strm_out = Decoupled(UInt(width = BISMOLimits.swuDescrOutBits))
    val rst_n = Bool(INPUT)
    strm_in.bits.setName("strm_in_V_V_TDATA")
    strm_in.valid.setName("strm_in_V_V_TVALID")
    strm_in.ready.setName("strm_in_V_V_TREADY")
    strm_out.bits.setName("strm_out_V_V_TDATA")
    strm_out.valid.setName("strm_out_V_V_TVALID")
    strm_out.ready.setName("strm_out_V_V_TREADY")
    rst_n.setName("ap_rst_n")
  }
  // clock needs to be added manually to BlackBox
	addClock(Driver.implicitClock)
  renameClock("clk", "ap_clk")

  // no template args
  val hlsTemplateParams: Map[String, String] = Map()
}