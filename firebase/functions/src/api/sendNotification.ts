import {onRequest} from "firebase-functions/v2/https";
import {defineString} from "firebase-functions/params";
import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";

// 環境変数からAPIキーを取得
const API_KEY_PARAM = defineString("MYFUNCTION_APIKEY", {
    description: "API Key for authorizing requests to the sendNotification function.",
});

// HTTPリクエストで呼び出される関数
export const sendNotification = onRequest (
    {
        region: "asia-northeast1",
        secrets: [API_KEY_PARAM],
    },
    async (request, response) => {
        // CORSヘッダーの設定
        response.set("Access-Control-Allow-Origin", "*");
        response.set("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.set("Access-Control-Headers", "Content-Type, Authorization");
    
        // OPTIONSリクエストへの対応
        if (request.method === "OPTIONS") {
            response.status(204).send("");
            return;
        }

        // POSTリクエスト以外は許可しない
        if (request.method !== "POST") {
            response.status(405).send({ success: false, error: "Method Not Allowed. Only POST is accepted."});
            return;
        }

        // APIキーによる認証
        const expectedApiKey = API_KEY_PARAM.value();
        const authorizationHeader = request.headers.authorization;

        if (!expectedApiKey) {
            logger.error("CRITICAL: API Key is not configured in Cloud Function environment variables.");
            response.status(500).send({ success: false, error: "Internal Server Error: API Key misconfiguration." });
            return;
        }

        if (!authorizationHeader || !authorizationHeader.startsWith("Bearer ") || authorizationHeader.split("Bearer ")[1] !== expectedApiKey) {
            logger.warn("Unauthorized access attempt.", {
            headers: request.headers,
            remoteAddress: request.ip,
            });
            response.status(401).send({ success: false, error: "Unauthorized: Invalid or missing API Key." });
            return;
        }

        // リクエストボディから必要なデータを取得
        const {
            processId,
            status,
            messageTitle,
            messageBody,
            deviceToken,
            taskName,
        } = request.body;

        // 必須パラメータのチェック (processId, status, deviceToken)
        if (!processId || !status || !deviceToken) {
            logger.error("Missing required fields in request body:", request.body);
            response.status(400).send({
            success: false,
            error: "Bad Request: Missing one or more required fields (processId, status, deviceToken).",
            });
            return;
        }

        // 時刻情報のサーバーサイドでの生成・割り当て
        const currentTimeString = new Date().getTime().toString();
        let taskActualStartTimeForPayload: string | undefined;
        let taskActualCompletionTimeForPayload: string | undefined;

        // statusの値に基づいて開始時刻か終了時刻か割り当てる
        const caseStatus = String(status);
        
        if (caseStatus === "START") {
            taskActualStartTimeForPayload = currentTimeString;
        } else if (caseStatus === "FINISH") {
            taskActualCompletionTimeForPayload = currentTimeString;
        }

        // 通知ペイロードの準備
        const fcmMessageData: { [key: string]: string } = {
            processId: String(processId),
            status: String(status),
            messageTitle: messageTitle ? String(messageTitle) : `${taskName || "タスク"} ${status}`,
            messageBody: messageBody ? String(messageBody) : `タスク (ID: ${String(processId).substring(0, 8)}) が ${status} になりました。`,
        };

        if (taskName) {
            fcmMessageData.taskName = String(taskName);
        }
        if (taskActualStartTimeForPayload) {
            fcmMessageData.taskActualStartTime = taskActualStartTimeForPayload;
        }
        if (taskActualCompletionTimeForPayload) {
            fcmMessageData.taskActualCompletionTime = taskActualCompletionTimeForPayload;
        }

        const fcmMessagePayload: admin.messaging.Message = {
            data: fcmMessageData,
            token: String(deviceToken),
        };

        logger.info("Attempting to send FCM message:", { fcmMessagePayload });

        // FCMメッセージの送信
        try {
            const fcmResponse = await admin.messaging().send(fcmMessagePayload);
            logger.info(`Successfully sent message for processId ${processId}:`, fcmResponse);
            response.status(200).send({ success: true, messageId: fcmResponse });
        } catch (error: any) {
            logger.error(`Error sending FCM message for processId ${processId}:`, error, {
                errorCode: error.code,
                errorMessage: error.message,
            });
            if (error.code === "messaging/registration-token-not-registered") {
                response.status(400).send({ success: false, error: "Invalid or unregistered FCM token.", details: error.message });
            } else if (error.code === "messaging/invalid-argument") {
                response.status(400).send({ success: false, error: "Invalid argument in FCM message.", details: error.message });
            } else {
                response.status(500).send({ success: false, error: "Internal Server Error while sending FCM.", details: error.message });
            }
        }
    }
);