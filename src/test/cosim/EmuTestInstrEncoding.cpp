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

#include <iostream>
#include "platform.h"
#include "EmuTestInstrEncoding.hpp"
#include <bitset>
#include "BISMOInstruction.hpp"
#include <string.h>

// Cosim test for checking instruction encoding

using namespace std;

WrapperRegDriver * p;
EmuTestInstrEncoding * t;

int main()
{
  bool t_okay = true;
  try {
    p = initPlatform();
    t = new EmuTestInstrEncoding(p);


    BISMOSyncInstruction sw_ins_sync, hw_ins_sync;
    /*for(int i = 0; i < 4; i++) {
      cout << "raw " << i << " " << bitset<32>(sw_ins_sync.raw[i]) << endl;
    }*/
    sw_ins_sync.isRunCfg = 1;
    sw_ins_sync.targetStage = 2;
    sw_ins_sync.isSendToken = 0;
    sw_ins_sync.chanID = 3;
    /*for(int i = 0; i < 4; i++) {
      cout << "raw " << i << " " << bitset<32>(sw_ins_sync.raw[i]) << endl;
    }*/

    // BitFieldMember uses big-endian packing but
    // Chisel expects little-endian, so swap storage order here
    t->set_raw_instr_in0(sw_ins_sync.raw[3]);
    t->set_raw_instr_in1(sw_ins_sync.raw[2]);
    t->set_raw_instr_in2(sw_ins_sync.raw[1]);
    t->set_raw_instr_in3(sw_ins_sync.raw[0]);

    hw_ins_sync.isRunCfg = t->get_sync_instr_out_isRunCfg();
    hw_ins_sync.targetStage = t->get_sync_instr_out_targetStage();
    hw_ins_sync.isSendToken = t->get_sync_instr_out_isSendToken();
    hw_ins_sync.chanID = t->get_sync_instr_out_chanID();

    bool sync_ok = (memcmp(sw_ins_sync.raw, hw_ins_sync.raw, 16) == 0);
    cout << "Sync instruction encoding: " << sync_ok << endl;
    t_okay &= sync_ok;

    // test fetch runcfg instructions
    BISMOFetchRunInstruction sw_ins_fetch, hw_ins_fetch;
    sw_ins_fetch.isRunCfg = 1;
    sw_ins_fetch.targetStage = 0;
    sw_ins_fetch.dram_base = 0xdead;
    sw_ins_fetch.dram_block_size_bytes = 0xbeef;
    sw_ins_fetch.dram_block_offset_bytes = 0xfeed;
    sw_ins_fetch.dram_block_count = 0xdeaf;
    sw_ins_fetch.tiles_per_row = 0xb00b;
    sw_ins_fetch.bram_addr_base = 0xd00d;
    sw_ins_fetch.bram_id_start = 10;
    sw_ins_fetch.bram_id_range = 20;

    hw_ins_fetch.isRunCfg = t->get_fr_instr_out_isRunCfg();
    hw_ins_fetch.targetStage = t->get_fr_instr_out_targetStage();
    hw_ins_fetch.dram_base = t->get_fr_instr_out_runcfg_dram_base();
    hw_ins_fetch.dram_block_size_bytes = t->get_fr_instr_out_runcfg_dram_block_size_bytes();
    hw_ins_fetch.dram_block_offset_bytes = t->get_fr_instr_out_runcfg_dram_block_offset_bytes();
    hw_ins_fetch.dram_block_count = t->get_fr_instr_out_runcfg_dram_block_count();
    hw_ins_fetch.tiles_per_row = t->get_fr_instr_out_runcfg_tiles_per_row();
    hw_ins_fetch.bram_addr_base = t->get_fr_instr_out_runcfg_bram_addr_base();
    hw_ins_fetch.bram_id_start = t->get_fr_instr_out_runcfg_bram_id_start();
    hw_ins_fetch.bram_id_range = t->get_fr_instr_out_runcfg_bram_id_range();

    bool fr_ok = (memcmp(sw_ins_fetch.raw, hw_ins_fetch.raw, 16) == 0);
    cout << "Fetch instruction encoding: " << fr_ok << endl;
    t_okay &= fr_ok;

    if(t_okay) {
      cout << "All tests passed" << endl;
    } else {
      cout << "Some tests failed" << endl;
    }

    deinitPlatform(p);
  } catch(const char * e) {
    cout << "Exception: " << e << endl;
  }
  return t_okay ? 0 : -1;
}
