import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";

// Firebase Admin SDKの初期化
try{
    admin.initializeApp();
    logger.info("Firebase Admin SDK initialized successfully");
} catch (error) {
    logger.error("Error initializing Firebase Admin SDK: ", error);
}

export { sendNotification } from './api/sendNotification';