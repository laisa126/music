package com.aurora.music.data.scanner

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aurora.music.domain.repository.MusicRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Incremental library indexing in the background (spec Sections 7 and 10).
 * Heavy work is constrained so it never runs on a low battery.
 */
@HiltWorker
class LibraryScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: MusicRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = runCatching {
        repository.rescan(force = inputData.getBoolean(KEY_FORCE, false))
        Result.success()
    }.getOrElse { error ->
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }

    companion object {
        const val KEY_FORCE = "force"
        private const val ONE_TIME_NAME = "aurora_scan_once"
        private const val PERIODIC_NAME = "aurora_scan_periodic"

        fun runNow(context: Context, force: Boolean = false) {
            val request = OneTimeWorkRequestBuilder<LibraryScanWorker>()
                .setInputData(androidx.work.workDataOf(KEY_FORCE to force))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(false)
                        .build(),
                )
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(ONE_TIME_NAME, ExistingWorkPolicy.KEEP, request)
        }

        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<LibraryScanWorker>(12, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .setRequiresDeviceIdle(false)
                        .build(),
                )
                .setBackoffCriteria(androidx.work.BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun cancelPeriodic(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_NAME)
        }
    }
}
