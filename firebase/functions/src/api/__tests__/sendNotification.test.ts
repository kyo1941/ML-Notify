import { sendNotification } from '../sendNotification';
import { logger } from 'firebase-functions';

// モックの設定
const mockSend = jest.fn();

jest.mock('firebase-admin', () => {
  const actualAdmin = jest.requireActual('firebase-admin');
  return {
    ...actualAdmin,
    messaging: () => ({
      send: mockSend,
    }),
  };
});

jest.mock('firebase-functions/logger', () => ({
  info: jest.fn(),
  warn: jest.fn(),
  error: jest.fn(),
  debug: jest.fn(),
}));

jest.mock('firebase-functions/params', () => ({
  ...jest.requireActual('firebase-functions/params'),
  defineString: jest.fn().mockImplementation((envKey) => {
    return {
      value: () => process.env[envKey],
    };
  }),
}));

describe('sendNotification Cloud Function', () => {
  const VALID_API_KEY = 'test-api-key';
  const ENV_API_KEY_NAME = 'SENDNOTIFICATION_APIKEY';
  const MOCK_TIMESTAMP = 1678886400000;
  const MOCK_TIMESTAMP_STRING = String(MOCK_TIMESTAMP);

  let mockReq: any;
  let mockRes: any;

  beforeEach(() => {
    // 各テストの前にモックをクリア
    mockSend.mockClear();
    (logger.info as jest.Mock).mockClear();
    (logger.warn as jest.Mock).mockClear();
    (logger.error as jest.Mock).mockClear();

    process.env[ENV_API_KEY_NAME] = VALID_API_KEY;

    // Jest の Fake Timers を使用してシステム時刻を固定
    jest.useFakeTimers();
    jest.setSystemTime(new Date(MOCK_TIMESTAMP));

      mockReq = {
        method: 'POST',
        headers: {
          'content-type': 'application/json',
          authorization: `Bearer ${VALID_API_KEY}`,
        },
        body: {
          processId: 'testProcessId',
          status: 'START',
          deviceToken: 'mockDeviceToken',
          messageTitle: 'Test Title',
          messageBody: 'Test Body',
          taskName: 'Test Task',
        },
        ip: '127.0.0.1',
      };
      mockRes = {
        set: jest.fn(),
        status: jest.fn().mockReturnThis(),
        send: jest.fn(),
      };
    });

    afterEach(() => {
      delete process.env[ENV_API_KEY_NAME];
      jest.useRealTimers();
    });

    // 正常系のテスト
    describe('Successful Operations', () => {
      it('should send an FCM message and return 200 on success', async () => {
        const mockFcmResponse = { messageId: 'mock-message-id-123' };
        mockSend.mockResolvedValue(mockFcmResponse);

        await sendNotification(mockReq, mockRes);

        expect(mockRes.status).toHaveBeenCalledWith(200);
        expect(mockRes.send).toHaveBeenCalledWith({ success: true, messageId: mockFcmResponse });
        expect(mockSend).toHaveBeenCalledTimes(1);
        expect(mockSend).toHaveBeenCalledWith(expect.objectContaining({
          token: 'mockDeviceToken',
          data: expect.objectContaining({
            processId: 'testProcessId',
            status: 'START',
            taskName: 'Test Task',
          }),
        }));
        expect(logger.info).toHaveBeenCalledWith('Attempting to send FCM message:', expect.any(Object));
        expect(logger.info).toHaveBeenCalledWith(`Successfully sent message for processId testProcessId:`, mockFcmResponse);
      });
    });

    // 認証と認可に関するテスト
    describe('Authentication and Authorization', () => {
      it('should return 401 if API key is invalid', async () => {
        mockReq.headers.authorization = 'Bearer invalid-api-key';

        await sendNotification(mockReq, mockRes);

        expect(mockRes.status).toHaveBeenCalledWith(401);
        expect(mockRes.send).toHaveBeenCalledWith({ success: false, error: 'Unauthorized: Invalid or missing API Key.' });
        expect(logger.warn).toHaveBeenCalledWith("Unauthorized access attempt.", expect.any(Object));
        expect(mockSend).not.toHaveBeenCalled();
      });

      it('should return 401 if Authorization header is missing', async () => {
        delete mockReq.headers.authorization;

        await sendNotification(mockReq, mockRes);

        expect(mockRes.status).toHaveBeenCalledWith(401);
        expect(mockRes.send).toHaveBeenCalledWith({ success: false, error: 'Unauthorized: Invalid or missing API Key.' });
        expect(mockSend).not.toHaveBeenCalled();
      });

      it('should return 500 if API key is not configured in environment', async () => {
        delete process.env[ENV_API_KEY_NAME]; // APIキーを環境変数から削除

        await sendNotification(mockReq, mockRes);

        expect(mockRes.status).toHaveBeenCalledWith(500);
        expect(mockRes.send).toHaveBeenCalledWith({ success: false, error: 'Internal Server Error: API Key misconfiguration.' });
        expect(logger.error).toHaveBeenCalledWith("CRITICAL: API Key is not configured in Cloud Function environment variables.");
        expect(mockSend).not.toHaveBeenCalled();
      });
    });

    // リクエストバリデーションのテスト
    describe('Request Validation', () => {
      it('should return 400 if processId is missing', async () => {
        delete mockReq.body.processId;
        await sendNotification(mockReq, mockRes);
        expect(mockRes.status).toHaveBeenCalledWith(400);
        expect(mockRes.send).toHaveBeenCalledWith(
          expect.objectContaining({
            error: 'Bad Request: Invalid request body.',
            details: expect.any(Object),
          })
        );
        expect(logger.error).toHaveBeenCalledWith(
          'Invalid request body:',
          expect.any(Object)
        );
      });

      it('should return 400 if status is missing', async () => {
        delete mockReq.body.status;
        await sendNotification(mockReq, mockRes);
        expect(mockRes.status).toHaveBeenCalledWith(400);
        expect(mockRes.send).toHaveBeenCalledWith(
          expect.objectContaining({
            error: 'Bad Request: Invalid request body.',
            details: expect.any(Object),
          })
        );
      });

      it('should return 400 if deviceToken is missing', async () => {
        delete mockReq.body.deviceToken;
        await sendNotification(mockReq, mockRes);
        expect(mockRes.status).toHaveBeenCalledWith(400);
        expect(mockRes.send).toHaveBeenCalledWith(
          expect.objectContaining({
            error: 'Bad Request: Invalid request body.',
            details: expect.any(Object),
          })
        );
      });
    });

    // HTTPメソッドとCORSのテスト
    describe('HTTP Method and CORS Handling', () => {
      it('should handle OPTIONS requests for CORS preflight and return 204', async () => {
        mockReq.method = 'OPTIONS';
        // OPTIONSリクエストでは通常 body や authorization は不要だが、テスト対象関数がそれを前提にエラーを出さないか確認
        delete mockReq.body;
        delete mockReq.headers.authorization;


        await sendNotification(mockReq, mockRes);

        expect(mockRes.status).toHaveBeenCalledWith(204);
        expect(mockRes.send).toHaveBeenCalledWith('');
        expect(mockRes.set).toHaveBeenCalledWith('Access-Control-Allow-Origin', '*');
        expect(mockRes.set).toHaveBeenCalledWith('Access-Control-Allow-Methods', 'POST, OPTIONS');
        expect(mockRes.set).toHaveBeenCalledWith('Access-Control-Headers', 'Content-Type, Authorization');
      });

      it('should return 405 for GET requests', async () => {
        mockReq.method = 'GET';
        await sendNotification(mockReq, mockRes);
        expect(mockRes.status).toHaveBeenCalledWith(405);
        expect(mockRes.send).toHaveBeenCalledWith({ success: false, error: 'Method Not Allowed. Only POST is accepted.' });
      });

      it('should return 405 for PUT requests', async () => {
        mockReq.method = 'PUT';
        await sendNotification(mockReq, mockRes);
        expect(mockRes.status).toHaveBeenCalledWith(405);
        expect(mockRes.send).toHaveBeenCalledWith({ success: false, error: 'Method Not Allowed. Only POST is accepted.' });
      });
    });

    // FCM送信エラーのシミュレーション
    describe('FCM Send Error Simulation', () => {
      it('should return 400 if FCM token is not registered (simulated)', async () => {
        const fcmError = new Error("Simulated FCM error: Token not registered");
        (fcmError as any).code = "messaging/registration-token-not-registered";
        mockSend.mockRejectedValue(fcmError);

        await sendNotification(mockReq, mockRes);

        expect(mockRes.status).toHaveBeenCalledWith(400);
        expect(mockRes.send).toHaveBeenCalledWith({ success: false, error: "Invalid or unregistered FCM token.", details: fcmError.message });
        expect(logger.error).toHaveBeenCalledWith(`Error sending FCM message for processId ${mockReq.body.processId}:`, fcmError, expect.any(Object));
        expect(mockSend).toHaveBeenCalledTimes(1);
      });

      it('should return 400 for invalid FCM arguments (simulated)', async () => {
        const fcmError = new Error("Simulated FCM error: Invalid Argument");
        (fcmError as any).code = "messaging/invalid-argument";
        mockSend.mockRejectedValue(fcmError);

        await sendNotification(mockReq, mockRes);

        expect(mockRes.status).toHaveBeenCalledWith(400);
        expect(mockRes.send).toHaveBeenCalledWith({ success: false, error: "Invalid argument in FCM message.", details: fcmError.message });
        expect(logger.error).toHaveBeenCalledWith(`Error sending FCM message for processId ${mockReq.body.processId}:`, fcmError, expect.any(Object));
      });

      it('should return 500 for other FCM sending errors (simulated)', async () => {
        const fcmError = new Error("Simulated generic FCM error");
        (fcmError as any).code = "messaging/some-other-unknown-error";
        mockSend.mockRejectedValue(fcmError);

        await sendNotification(mockReq, mockRes);

        expect(mockRes.status).toHaveBeenCalledWith(500);
        expect(mockRes.send).toHaveBeenCalledWith({ success: false, error: "Internal Server Error while sending FCM.", details: fcmError.message });
        expect(logger.error).toHaveBeenCalledWith(`Error sending FCM message for processId ${mockReq.body.processId}:`, fcmError, expect.any(Object));
      });
    });

    // 開始/終了時刻の割り当てのテスト
    describe('Timestamp Assignment in Payload', () => {
      it('should include taskActualStartTime with mocked timestamp when status is START', async () => {
        mockReq.body.status = 'START';
        mockSend.mockResolvedValue({ messageId: 'fcm-success-start' });

        await sendNotification(mockReq, mockRes);

        expect(mockRes.status).toHaveBeenCalledWith(200);
        expect(mockSend).toHaveBeenCalledTimes(1);
        expect(mockSend).toHaveBeenCalledWith(expect.objectContaining({
          data: expect.objectContaining({
            taskActualStartTime: MOCK_TIMESTAMP_STRING,
          }),
        }));
        // taskActualCompletionTime が含まれないことも確認
        const sentData = mockSend.mock.calls[0][0].data;
        expect(sentData.taskActualCompletionTime).toBeUndefined();
      });

      it('should include taskActualCompletionTime with mocked timestamp when status is COMPLETED', async () => {
        mockReq.body.status = 'COMPLETED';
        mockSend.mockResolvedValue({ messageId: 'fcm-success-completed' });

        await sendNotification(mockReq, mockRes);

        expect(mockRes.status).toHaveBeenCalledWith(200);
        expect(mockSend).toHaveBeenCalledTimes(1);
        expect(mockSend).toHaveBeenCalledWith(expect.objectContaining({
          data: expect.objectContaining({
            taskActualCompletionTime: MOCK_TIMESTAMP_STRING,
          }),
        }));
        const sentData = mockSend.mock.calls[0][0].data;
        expect(sentData.taskActualStartTime).toBeUndefined();
      });

      it('should include taskActualCompletionTime with mocked timestamp when status is FAILED', async () => {
        mockReq.body.status = 'FAILED';
        mockSend.mockResolvedValue({ messageId: 'fcm-success-failed' });

        await sendNotification(mockReq, mockRes);

        expect(mockRes.status).toHaveBeenCalledWith(200);
        expect(mockSend).toHaveBeenCalledTimes(1);
        expect(mockSend).toHaveBeenCalledWith(expect.objectContaining({
          data: expect.objectContaining({
            taskActualCompletionTime: MOCK_TIMESTAMP_STRING,
          }),
        }));
        const sentData = mockSend.mock.calls[0][0].data;
        expect(sentData.taskActualStartTime).toBeUndefined();
      });

      it('should not include taskActualStartTime or taskActualCompletionTime when status is neither START nor COMPLETED nor FAILED', async () => {
        mockReq.body.status = 'UNKNOWN';
        await sendNotification(mockReq, mockRes);
        expect(mockRes.status).toHaveBeenCalledWith(400);
        expect(mockRes.send).toHaveBeenCalledWith(
          expect.objectContaining({
            error: 'Bad Request: Invalid request body.',
            details: expect.any(Object),
          })
        );
        // FCM送信は呼ばれない
        expect(mockSend).not.toHaveBeenCalled();
      });
    });
});