<script setup lang="ts">
import { computed } from 'vue';
import { useDroneStore } from '../store/drone';

const store = useDroneStore();

const shootingWaypointCount = computed(
  () => store.waypoints.filter((w) => w.action === 'photo' || w.action === 'video').length
);

const result = computed(() => store.coverageResult);

function coverageColor(pct: number): string {
  if (pct >= 90) return '#22c55e';
  if (pct >= 70) return '#eab308';
  return '#ef4444';
}

function formatArea(m2: number): string {
  if (m2 >= 1000000) return `${(m2 / 1000000).toFixed(2)} km²`;
  return `${(m2 / 1000).toFixed(1)} 千 m²`;
}

function handleCheck() {
  store.runCoverageCheck();
}
</script>

<template>
  <div class="bg-slate-800 rounded-lg p-4 space-y-3">
    <h3 class="text-sm font-bold text-slate-200 border-b border-slate-700 pb-2">
      拍摄覆盖校验
    </h3>

    <!-- Hint -->
    <p class="text-[10px] text-slate-400 leading-relaxed">
      根据航点动作（photo/video）计算地面拍摄足迹，在航点包围盒内网格采样，
      检查是否存在漏拍区域并定位需要补飞的航线区段。
    </p>

    <!-- Camera FOV config -->
    <div class="flex items-center justify-between text-[10px] text-slate-400">
      <span>相机水平视场角</span>
      <div class="flex items-center gap-1">
        <input
          type="number"
          min="10"
          max="170"
          step="1"
          v-model.number="store.droneConfig.cameraFov"
          class="w-14 bg-slate-900 border border-slate-700 rounded px-1 py-0.5 text-slate-200 text-right"
        />
        <span>°</span>
      </div>
    </div>

    <button
      @click="handleCheck"
      :disabled="store.waypoints.length === 0"
      class="w-full py-2 rounded text-xs font-medium bg-cyan-700 text-white hover:bg-cyan-600 disabled:opacity-40 disabled:cursor-not-allowed transition"
    >
      🔍 校验覆盖
    </button>

    <!-- No shooting waypoints -->
    <div
      v-if="result && result.footprintCount === 0"
      class="bg-amber-900/40 border border-amber-700 rounded p-2 text-[11px] text-amber-200"
    >
      ⚠ 未发现 photo/video 动作航点，目标区域无法被覆盖。请为航点设置拍摄动作后再校验。
    </div>

    <!-- Result -->
    <div v-if="result && result.footprintCount > 0" class="space-y-3">
      <!-- Coverage percent -->
      <div>
        <div class="flex justify-between text-xs text-slate-300 mb-1">
          <span>覆盖率</span>
          <span class="font-bold" :style="{ color: coverageColor(result.coveragePercent) }">
            {{ result.coveragePercent.toFixed(1) }}%
          </span>
        </div>
        <div class="w-full bg-slate-700 rounded-full h-2.5">
          <div
            class="h-2.5 rounded-full transition-all"
            :style="{
              width: result.coveragePercent + '%',
              backgroundColor: coverageColor(result.coveragePercent),
            }"
          />
        </div>
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-2 gap-2 text-xs">
        <div class="bg-slate-900 rounded p-2">
          <div class="text-slate-400">拍摄足迹</div>
          <div class="text-base font-bold text-sky-400">{{ result.footprintCount }}</div>
        </div>
        <div class="bg-slate-900 rounded p-2">
          <div class="text-slate-400">漏拍区段</div>
          <div class="text-base font-bold text-red-400">{{ result.gaps.length }}</div>
        </div>
        <div class="bg-slate-900 rounded p-2">
          <div class="text-slate-400">已覆盖采样</div>
          <div class="text-base font-bold text-green-400">{{ result.coveredSamples }}</div>
        </div>
        <div class="bg-slate-900 rounded p-2">
          <div class="text-slate-400">未覆盖采样</div>
          <div class="text-base font-bold text-amber-400">{{ result.uncoveredSamples }}</div>
        </div>
      </div>

      <!-- Gaps -->
      <div v-if="result.gaps.length > 0" class="space-y-2">
        <h4 class="text-xs text-slate-300 font-semibold flex items-center gap-1">
          🚁 需要补飞的区段
        </h4>
        <div
          v-for="gap in result.gaps"
          :key="gap.id"
          class="bg-slate-900 border border-red-800/60 rounded p-2 space-y-1"
        >
          <div class="flex items-center justify-between">
            <span class="text-[11px] font-bold text-red-300">
              区段 WP{{ gap.reflight.fromIndex + 1 }}
              <template v-if="gap.reflight.fromIndex !== gap.reflight.toIndex">
                → WP{{ gap.reflight.toIndex + 1 }}
              </template>
            </span>
            <span class="text-[10px] text-slate-400">
              {{ formatArea(gap.approxArea) }}
            </span>
          </div>
          <div class="text-[10px] text-slate-400 leading-relaxed">{{ gap.message }}</div>
          <div class="text-[10px] text-slate-500">
            中心: {{ gap.centroidLat.toFixed(5) }}, {{ gap.centroidLng.toFixed(5) }}
            · 距航线 {{ gap.distanceToRoute.toFixed(0) }} m
          </div>
        </div>
      </div>

      <div
        v-else
        class="bg-green-900/40 border border-green-700 rounded p-2 text-[11px] text-green-200"
      >
        ✓ 目标区域已完整覆盖，无需补飞。
      </div>
    </div>

    <!-- Initial empty state -->
    <div
      v-if="!result && store.waypoints.length > 0 && shootingWaypointCount === 0"
      class="text-[11px] text-slate-500"
    >
      当前航点无拍摄动作（photo/video），点击校验将提示漏拍。
    </div>
  </div>
</template>
