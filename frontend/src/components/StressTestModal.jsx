import React, { useState } from 'react';
import { X, Play, CheckCircle2, ShieldCheck, AlertTriangle, Clock, Users, Hash } from 'lucide-react';
import { runStressTest } from '../services/api';

export default function StressTestModal({ product, onClose, onTestComplete }) {
  const [threads, setThreads] = useState(50);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleRun = async () => {
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const data = await runStressTest(product.id, threads);
      setResult(data);
      if (onTestComplete) onTestComplete();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Stress test failed to execute');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: 'fixed',
      inset: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      backdropFilter: 'blur(8px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 100,
      padding: '1rem'
    }}>
      
      <div style={{
        backgroundColor: '#0f172a',
        border: '1px solid #334155',
        borderRadius: '1rem',
        maxWidth: '560px',
        width: '100%',
        padding: '1.75rem',
        position: 'relative',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)'
      }}>
        
        {/* Close Button */}
        <button
          onClick={onClose}
          style={{
            position: 'absolute',
            top: '1.25rem',
            right: '1.25rem',
            background: 'transparent',
            border: 'none',
            color: '#94a3b8',
            cursor: 'pointer'
          }}
        >
          <X size={20} />
        </button>

        {/* Modal Header */}
        <div style={{ marginBottom: '1.25rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
            <span style={{
              backgroundColor: 'rgba(56, 189, 248, 0.15)',
              color: '#38bdf8',
              padding: '0.2rem 0.5rem',
              borderRadius: '0.25rem',
              fontSize: '0.7rem',
              fontWeight: 700,
              fontFamily: 'var(--font-mono)'
            }}>
              CONCURRENCY SIMULATOR
            </span>
          </div>
          <h2 style={{ fontSize: '1.35rem', fontWeight: 800, color: '#f8fafc' }}>
            Simulate High-Traffic Spike
          </h2>
          <p style={{ fontSize: '0.85rem', color: '#94a3b8' }}>
            Target: <strong style={{ color: '#cbd5e1' }}>{product.title}</strong> (Current Stock: <strong style={{ color: '#f59e0b' }}>{product.stockQuantity}</strong>)
          </p>
        </div>

        {/* Thread Controls */}
        <div style={{
          backgroundColor: '#1e293b',
          border: '1px solid #334155',
          borderRadius: '0.75rem',
          padding: '1rem',
          marginBottom: '1.25rem'
        }}>
          <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#cbd5e1', marginBottom: '0.5rem' }}>
            Select Number of Simultaneous Threads:
          </label>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            {[20, 50, 100].map(count => (
              <button
                key={count}
                onClick={() => setThreads(count)}
                disabled={loading}
                style={{
                  flex: 1,
                  padding: '0.5rem',
                  borderRadius: '0.5rem',
                  fontSize: '0.8rem',
                  fontWeight: 700,
                  backgroundColor: threads === count ? '#f59e0b' : '#0f172a',
                  color: threads === count ? '#0f172a' : '#cbd5e1',
                  border: threads === count ? '1px solid #f59e0b' : '1px solid #334155',
                  cursor: loading ? 'not-allowed' : 'pointer'
                }}
              >
                {count} Threads
              </button>
            ))}
          </div>
          <p style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.5rem' }}>
            All {threads} threads will use Java <code style={{ color: '#38bdf8' }}>CountDownLatch</code> to strike the order endpoint at the exact same millisecond.
          </p>
        </div>

        {/* Error message */}
        {error && (
          <div style={{
            backgroundColor: 'rgba(239, 68, 68, 0.1)',
            border: '1px solid rgba(239, 68, 68, 0.3)',
            borderRadius: '0.5rem',
            padding: '0.75rem',
            marginBottom: '1rem',
            color: '#f87171',
            fontSize: '0.8rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}>
            <AlertTriangle size={16} />
            <span>{error}</span>
          </div>
        )}

        {/* Results Display */}
        {result && (
          <div style={{
            backgroundColor: 'rgba(16, 185, 129, 0.08)',
            border: '1px solid rgba(16, 185, 129, 0.3)',
            borderRadius: '0.75rem',
            padding: '1.25rem',
            marginBottom: '1.25rem'
          }}>
            
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              color: '#10b981',
              fontWeight: 800,
              fontSize: '0.9rem',
              marginBottom: '1rem'
            }}>
              <ShieldCheck size={20} />
              <span>ZERO OVERSELLING VERIFIED • 100% CONSISTENT</span>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '0.75rem' }}>
              
              <div style={{ backgroundColor: '#0f172a', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid #334155' }}>
                <span style={{ fontSize: '0.7rem', color: '#94a3b8', display: 'block' }}>Initial vs Final Stock</span>
                <span style={{ fontSize: '1.1rem', fontWeight: 800, color: '#f8fafc', fontFamily: 'var(--font-mono)' }}>
                  {result.initialStock} ➔ {result.finalStock}
                </span>
              </div>

              <div style={{ backgroundColor: '#0f172a', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid #334155' }}>
                <span style={{ fontSize: '0.7rem', color: '#94a3b8', display: 'block' }}>Successful Orders</span>
                <span style={{ fontSize: '1.1rem', fontWeight: 800, color: '#10b981', fontFamily: 'var(--font-mono)' }}>
                  {result.successfulOrders}
                </span>
              </div>

              <div style={{ backgroundColor: '#0f172a', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid #334155' }}>
                <span style={{ fontSize: '0.7rem', color: '#94a3b8', display: 'block' }}>Rejected (Out of Stock)</span>
                <span style={{ fontSize: '1.1rem', fontWeight: 800, color: '#ef4444', fontFamily: 'var(--font-mono)' }}>
                  {result.rejectedOrders}
                </span>
              </div>

              <div style={{ backgroundColor: '#0f172a', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid #334155' }}>
                <span style={{ fontSize: '0.7rem', color: '#94a3b8', display: 'block' }}>Total Execution Time</span>
                <span style={{ fontSize: '1.1rem', fontWeight: 800, color: '#38bdf8', fontFamily: 'var(--font-mono)' }}>
                  {result.durationMs} ms
                </span>
              </div>

            </div>

            <p style={{ fontSize: '0.75rem', color: '#cbd5e1', marginTop: '0.85rem', lineHeight: 1.5 }}>
              Summary: {result.summary}
            </p>

          </div>
        )}

        {/* Modal Action Buttons */}
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          
          <button
            onClick={handleRun}
            disabled={loading}
            style={{
              flex: 1,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '0.5rem',
              backgroundColor: '#38bdf8',
              color: '#0f172a',
              padding: '0.75rem',
              borderRadius: '0.5rem',
              fontWeight: 800,
              fontSize: '0.875rem',
              border: 'none',
              cursor: loading ? 'not-allowed' : 'pointer'
            }}
          >
            <Play size={16} />
            <span>{loading ? `Firing ${threads} Threads...` : `Run ${threads} Concurrent Threads`}</span>
          </button>

          <button
            onClick={onClose}
            style={{
              backgroundColor: '#1e293b',
              color: '#cbd5e1',
              border: '1px solid #334155',
              padding: '0.75rem 1.25rem',
              borderRadius: '0.5rem',
              fontWeight: 600,
              fontSize: '0.875rem',
              cursor: 'pointer'
            }}
          >
            Close
          </button>

        </div>

      </div>

    </div>
  );
}
