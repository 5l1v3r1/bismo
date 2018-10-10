#pragma once
#include <stdint.h>
#include "BISMOInstruction.hpp"
#include "hls_stream.h"
#include <cassert>

namespace InstrGen {

typedef struct {
  // number of tiles in a single binary matrix
  // expressed in terms of the instantiated DPA size
  uint16_t tiles_m;
  uint16_t tiles_k;
  uint16_t tiles_n;
  // number of bits in input matrices
  uint8_t bits_l;
  uint8_t bits_r;
  // signedness for the input matrices
  bool signed_l;
  bool signed_r;
  // base addresses for buffer accesses
  uint16_t base_l;
  uint16_t base_r;
  uint8_t base_res;
  // number of buffers for latency hiding
  uint8_t nbufs_res;
} SingleMMDescriptor;

// create the Execute stage instruction stream for a single bit-serial MM
void ExecInstrGenSingleMM(
  // desciptor for the MM operation
  SingleMMDescriptor in,
  // generated instructions will be placed here
  hls::stream<BISMOInstruction> & out
) {
  // single-bit signed is used to indicate bipolar (-1, +1) which is
  // currently not supported:
  assert(!(in.bits_l == 1 && in.signed_l));
  assert(!(in.bits_r == 1 && in.signed_r));
  BISMOInstruction ins;
  // all instructions are targeting the execute stage
  ins.sync.targetStage = stgExec;
  // start by acquiring input buffers
  ins.sync.isRunCfg = 0;
  ins.sync.isSendToken = 0;
  ins.sync.chanID = 0;
  out.write(ins);
  // keep track of which result buffer we wrote to last
  size_t offset_res = 0;
  for(size_t m = 0; m < in.tiles_m; m++) {
    for(size_t n = 0; n < in.tiles_n; n++) {
      // starting a new result tile:
      // acquire a result buffer
      ins.sync.isRunCfg = 0;
      ins.sync.isSendToken = 0;
      ins.sync.chanID = 1;
      out.write(ins);
      for(size_t l = 0; l < in.bits_l; l++) {
        for(size_t r = 0; r < in.bits_r; r++) {
          // helper variables based on current loop iteration
          bool tile_first = (l == 0) && (r == 0);
          bool tile_last = (l == in.bits_l-1) && (r == in.bits_r-1);
          size_t weight = l + r;
          // whether the current bit position is negative for
          // the input matrices
          bool neg_l = (l == in.bits_l-1) && in.signed_l;
          bool neg_r = (r == in.bits_r-1) && in.signed_r;
          bool negate = neg_l ^ neg_r;
          size_t offset_l = in.tiles_k * (m + l * in.tiles_m);
          size_t offset_r = in.tiles_k * (n + r * in.tiles_n);
          // switch result buffers for latency hiding
          offset_res = (offset_res + 1) % in.nbufs_res;
          ins.exec.isRunCfg = 1;
          ins.exec.lhsOffset = in.base_l + offset_l;
          ins.exec.rhsOffset = in.base_r + offset_r;
          ins.exec.numTiles = in.tiles_k;
          ins.exec.shiftAmount = weight;
          ins.exec.negate = negate ? 1 : 0;
          // clear accumulator on first iteration of this result tile
          ins.exec.clear_before_first_accumulation = tile_first ? 1 : 0;
          // write result on first iteration of this result tile
          ins.exec.writeEn = tile_last ? 1 : 0;
          ins.exec.writeAddr = in.base_res + offset_res;
          out.write(ins);
        }
      }
      // finished computing result tile
      // release the result buffer
      ins.sync.isRunCfg = 0;
      ins.sync.isSendToken = 1;
      ins.sync.chanID = 1;
      out.write(ins);
    }
  }
  // release the input buffers
  ins.sync.isRunCfg = 0;
  ins.sync.isSendToken = 1;
  ins.sync.chanID = 0;
  out.write(ins);
}

}