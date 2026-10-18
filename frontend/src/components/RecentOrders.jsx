import React from 'react';
import { ShoppingBag, CheckCircle, Clock, AlertTriangle, XCircle, RefreshCw } from 'lucide-react';

export default function RecentOrders({ orders }) {
  if (!orders || orders.length === 0) {
    return (
      <div style={{
        marginTop: '3rem',
        padding: '2rem',
        backgroundColor: '#111827',
        border: '1px solid #1f2937',
        borderRadius: '0.875rem',
        textAlign: 'center'
      }}>
        <ShoppingBag size={28} color="#64748b" style={{ margin: '0 auto 0.5rem auto' }} />
        <p style={{ color: '#94a3b8', fontSize: '0.9rem' }}>No orders placed yet. Click "⚡ Instant Flash Buy" on any product above!</p>
      </div>
    );
  }

  const getStatusBadge = (status) => {
    switch (status) {
      case 'CONFIRMED':
        return {
          icon: <CheckCircle size={12} />,
          color: '#10b981',
          bg: 'rgba(16, 185, 129, 0.1)'
        };
      case 'PENDING_PAYMENT':
      case 'PENDING':
        return {
          icon: <Clock size={12} />,
          color: '#f59e0b',
          bg: 'rgba(245, 158, 11, 0.1)'
        };
      case 'FAILED':
        return {
          icon: <XCircle size={12} />,
          color: '#ef4444',
          bg: 'rgba(239, 68, 68, 0.1)'
        };
      case 'EXPIRED':
        return {
          icon: <AlertTriangle size={12} />,
          color: '#f97316',
          bg: 'rgba(249, 115, 22, 0.1)'
        };
      case 'CANCELLED':
      case 'REFUNDED':
        return {
          icon: <RefreshCw size={12} />,
          color: '#a855f7',
          bg: 'rgba(168, 85, 247, 0.1)'
        };
      default:
        return {
          icon: <CheckCircle size={12} />,
          color: '#94a3b8',
          bg: 'rgba(148, 163, 184, 0.1)'
        };
    }
  };

  return (
    <div style={{
      marginTop: '3.5rem',
      backgroundColor: '#111827',
      border: '1px solid #1f2937',
      borderRadius: '0.875rem',
      padding: '1.5rem',
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.25)'
    }}>
      
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem' }}>
        <div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 800, color: '#f8fafc' }}>
            Live Orders &amp; Audit Trail
          </h2>
          <p style={{ fontSize: '0.8rem', color: '#64748b' }}>
            Real-time feed of purchases processed with Kafka event streaming &amp; distributed locks
          </p>
        </div>
        <span style={{
          fontSize: '0.75rem',
          color: '#10b981',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          border: '1px solid rgba(16, 185, 129, 0.25)',
          padding: '0.25rem 0.6rem',
          borderRadius: '9999px',
          fontWeight: 600
        }}>
          {orders.length} Logged Transactions
        </span>
      </div>

      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.85rem' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid #1e293b', color: '#64748b', fontSize: '0.75rem', textTransform: 'uppercase' }}>
              <th style={{ padding: '0.75rem 0.5rem' }}>Order Number</th>
              <th style={{ padding: '0.75rem 0.5rem' }}>Product</th>
              <th style={{ padding: '0.75rem 0.5rem' }}>User / Thread</th>
              <th style={{ padding: '0.75rem 0.5rem' }}>Amount</th>
              <th style={{ padding: '0.75rem 0.5rem' }}>Status</th>
              <th style={{ padding: '0.75rem 0.5rem' }}>Timestamp</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order, idx) => {
              const badge = getStatusBadge(order.status);
              return (
                <tr key={order.orderNumber || idx} style={{ borderBottom: '1px solid rgba(30, 41, 59, 0.5)' }}>
                  <td style={{ padding: '0.75rem 0.5rem', fontFamily: 'monospace', color: '#38bdf8', fontWeight: 600 }}>
                    {order.orderNumber}
                  </td>
                  <td style={{ padding: '0.75rem 0.5rem', color: '#f8fafc', fontWeight: 600 }}>
                    {order.productTitle}
                  </td>
                  <td style={{ padding: '0.75rem 0.5rem', color: '#94a3b8' }}>
                    {order.userEmail}
                  </td>
                  <td style={{ padding: '0.75rem 0.5rem', fontFamily: 'monospace', color: '#f59e0b', fontWeight: 700 }}>
                    ${order.totalAmount}
                  </td>
                  <td style={{ padding: '0.75rem 0.5rem' }}>
                    <span style={{
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '0.25rem',
                      color: badge.color,
                      backgroundColor: badge.bg,
                      padding: '0.2rem 0.5rem',
                      borderRadius: '0.25rem',
                      fontSize: '0.7rem',
                      fontWeight: 700
                    }}>
                      {badge.icon}
                      {order.status}
                    </span>
                  </td>
                  <td style={{ padding: '0.75rem 0.5rem', color: '#64748b', fontSize: '0.75rem' }}>
                    {order.createdAt ? new Date(order.createdAt).toLocaleTimeString() : 'Just now'}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

    </div>
  );
}
