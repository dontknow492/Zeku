package org.ghost.zeku.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters

class DownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        TODO("Not yet implemented")
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        TODO("Not yet implemented")
//        val workNotif = NotificationUtil(MyApplication.instance).createDefaultWorkerNotification()

//        return ForegroundInfo(
//            1000000000,
//            workNotif,
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
//            } else {
//                0
//            },
//        )
    }
}