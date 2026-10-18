import axios from 'axios';

const API_BASE = '/api/v1';

const apiClient = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const getProducts = async () => {
  const response = await apiClient.get('/products');
  return response.data;
};

export const resetProductStock = async (productId, stock = 10) => {
  const response = await apiClient.post(`/products/${productId}/reset-stock?stock=${stock}`);
  return response.data;
};

export const placeFlashBuyOrder = async (productId, userEmail = 'buyer@flashflow.io', idempotencyKey = null) => {
  const key = idempotencyKey || `idem-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
  const response = await apiClient.post(
    '/orders/flash-buy',
    {
      productId,
      quantity: 1,
      userEmail,
    },
    {
      headers: {
        'X-Idempotency-Key': key,
      },
    }
  );
  return response.data;
};

export const placeAsyncOrder = async (productId, userEmail = 'async_buyer@flashflow.io', idempotencyKey = null) => {
  const key = idempotencyKey || `idem-async-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
  const response = await apiClient.post(
    '/orders/checkout-async',
    {
      productId,
      quantity: 1,
      userEmail,
    },
    {
      headers: {
        'X-Idempotency-Key': key,
      },
    }
  );
  return response.data;
};

export const sendPaymentWebhook = async (webhookPayload) => {
  const response = await apiClient.post('/payments/webhook', webhookPayload, {
    headers: {
      'X-Webhook-Id': webhookPayload.webhookId,
    },
  });
  return response.data;
};

export const triggerReconciliation = async (timeoutMinutes = 2) => {
  const response = await apiClient.post(`/reconciliation/run?timeoutMinutes=${timeoutMinutes}`);
  return response.data;
};

export const getRecentOrders = async () => {
  const response = await apiClient.get('/orders/recent');
  return response.data;
};

export const runStressTest = async (productId, concurrentUsers = 50) => {
  const response = await apiClient.post('/stress-test/run', {
    productId,
    concurrentUsers,
  });
  return response.data;
};
