import * as admin from "firebase-admin";
import * as logger from "firebase-functions/logger";

const db = admin.firestore();

/**
 * タスク名からprocessIdを取得します。
 * @param taskName 検索するタスク名
 * @returns processId、見つからない場合はnull
 */


 // ここはTaskNameからdeviceNameを取得できるようにしたい．
export async function getTaskInfoByTaskName(taskName: string): Promise<string | null> {
    try {
        // taskName が一致するようなtasksコレクションのドキュメントを検索
        const snapshot = await db.collection("tasks")
                                .where("taskName", "==", taskName)
                                .limit(1)
                                .get();

        if (snapshot.empty) {
            logger.info(`No task found with name: ${taskName}`);
            return null;
        }

        const processId = snapshot.docs[0].id;
        logger.info(`Found processId: ${processId} for taskName: ${taskName}`);
        return processId;

    } catch (error) {
        logger.error(`Error fetching processId for taskName ${taskName}:`, error);
        throw error;
    }
}

// 以下にdeviceNameからdeviceTokenを取得する関数を追加したい

// 上記の二つの関数を実行して最終的にデータを整理して返せるようなmain関数を追加したい