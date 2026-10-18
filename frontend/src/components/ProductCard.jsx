import React, { useState } from 'react';
import { Zap, Activity, RotateCcw, AlertCircle, CheckCircle2 } from 'lucide-react';

export default function ProductCard({ product, onFlashBuy, onOpenStressTest, onResetStock, isBuying }) {
  const [resetting, setResetting] = useState(false);

  const isSoldOut = product.stockQuantity <= 0;
  const isLowStock = product.stockQuantity > 0 && product.stockQuantity <= 3;

  // Determine progress bar color
  let progressColor = '#10b981'; // Green
  if (product.percentageLeft <= 50) progressColor = '#f59e0b'; // Amber
  if (product.percentageLeft <= 25) progressColor = '#ef4444'; // Red

  const handleReset = async () => {
    setResetting(true);
    await onResetStock(product.id, 10);
    setResetting(false);
  };

  return (
    <div style={{
      backgroundColor: '#111827',
      border: isLowStock ? '1px solid rgba(239, 68, 68, 0.5)' : '1px solid #1f2937',
      borderRadius: '0.875rem',
      overflow: 'hidden',
      display: 'flex',
      flexDirection: 'column',
      transition: 'all 0.25s ease',
      boxShadow: isLowStock ? '0 0 20px rgba(239, 68, 68, 0.15)' : '0 4px 12px rgba(0, 0, 0, 0.25)'
    }}>
      
      {/* Product Image & Badges */}
      <div style={{ position: 'relative', height: '180px', overflow: 'hidden', backgroundColor: '#1e293b' }}>
        <img
          src={product.imageUrl}
          alt={product.title}
          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
          loading="lazy"
        />
        
        {/* Category Pill */}
        <span style={{
          position: 'absolute',
          top: '10px',
          left: '10px',
          backgroundColor: 'rgba(15, 23, 42, 0.8)',
          backdropFilter: 'blur(8px)',
          color: '#cbd5e1',
          fontSize: '0.7rem',
          fontWeight: 600,
          padding: '0.2rem 0.55rem',
          borderRadius: '0.375rem',
          border: '1px solid rgba(255, 255, 255, 0.1)'
        }}>
          {product.category}
        </span>

        {/* Urgency Badge */}
        {isSoldOut ? (
          <span style={{
            position: 'absolute',
            top: '10px',
            right: '10px',
            backgroundColor: '#ef4444',
            color: '#ffffff',
            fontSize: '0.75rem',
            fontWeight: 700,
            padding: '0.25rem 0.65rem',
            borderRadius: '0.375rem',
            textTransform: 'uppercase',
            boxShadow: '0 0 10px rgba(239, 68, 68, 0.5)'
          }}>
            Sold Out
          </span>
        ) : isLowStock ? (
          <span style={{
            position: 'absolute',
            top: '10px',
            right: '10px',
            backgroundColor: '#f59e0b',
            color: '#0f172a',
            fontSize: '0.75rem',
            fontWeight: 800,
            padding: '0.25rem 0.65rem',
            borderRadius: '0.375rem',
            textTransform: 'uppercase',
            display: 'flex',
            alignItems: 'center',
            gap: '0.25rem',
            boxShadow: '0 0 12px rgba(245, 158, 11, 0.5)'
          }}>
            <AlertCircle size={13} />
            Only {product.stockQuantity} Left!
          </span>
        ) : null}
      </div>

      {/* Card Content */}
      <div style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', flexGrow: 1 }}>
        
        <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', gap: '0.5rem', marginBottom: '0.35rem' }}>
          <h3 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#f8fafc', lineHeight: 1.3 }}>
            {product.title}
          </h3>
          <span style={{ fontSize: '1.2rem', fontWeight: 800, color: '#f59e0b', fontFamily: 'var(--font-mono)' }}>
            ${product.price}
          </span>
        </div>

        <p style={{ fontSize: '0.8rem', color: '#94a3b8', lineHeight: 1.5, marginBottom: '1.25rem', minHeight: '2.4rem' }}>
          {product.description}
        </p>

        {/* Live Stock Indicator & Progress Bar */}
        <div style={{ marginBottom: '1.25rem' }}>
          
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.75rem', marginBottom: '0.4rem' }}>
            <span style={{ color: '#cbd5e1', fontWeight: 500 }}>Live Available Stock:</span>
            <span style={{
              fontWeight: 700,
              fontFamily: 'var(--font-mono)',
              color: isSoldOut ? '#ef4444' : isLowStock ? '#f59e0b' : '#10b981'
            }}>
              {product.stockQuantity} / {product.initialStock} units ({product.percentageLeft}%)
            </span>
          </div>

          <div style={{ width: '100%', height: '7px', backgroundColor: '#1e293b', borderRadius: '9999px', overflow: 'hidden' }}>
            <div style={{
              width: `${Math.max(0, Math.min(100, product.percentageLeft))}%`,
              height: '100%',
              backgroundColor: progressColor,
              borderRadius: '9999px',
              transition: 'width 0.4s ease'
            }} />
          </div>

        </div>

        {/* Actions Button Cluster */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem', marginTop: 'auto' }}>
          
          {/* Main Flash Buy Action */}
          <button
            onClick={() => onFlashBuy(product.id)}
            disabled={isSoldOut || isBuying}
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '0.5rem',
              backgroundColor: isSoldOut ? '#334155' : '#f59e0b',
              color: isSoldOut ? '#94a3b8' : '#0f172a',
              padding: '0.65rem 1rem',
              borderRadius: '0.5rem',
              fontWeight: 800,
              fontSize: '0.875rem',
              border: 'none',
              cursor: isSoldOut || isBuying ? 'not-allowed' : 'pointer',
              transition: 'all 0.15s ease',
              boxShadow: isSoldOut ? 'none' : '0 4px 15px rgba(245, 158, 11, 0.3)'
            }}
          >
            <Zap size={16} />
            <span>{isSoldOut ? 'Sold Out' : isBuying ? 'Acquiring Lock...' : '⚡ Instant Flash Buy'}</span>
          </button>

          {/* Secondary Actions Row: Stress Test + Reset */}
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            
            <button
              onClick={() => onOpenStressTest(product)}
              style={{
                flex: 1,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '0.4rem',
                backgroundColor: 'rgba(56, 189, 248, 0.1)',
                border: '1px solid rgba(56, 189, 248, 0.3)',
                color: '#38bdf8',
                padding: '0.45rem 0.65rem',
                borderRadius: '0.5rem',
                fontSize: '0.75rem',
                fontWeight: 600,
                cursor: 'pointer'
              }}
            >
              <Activity size={13} />
              <span>Stress Test (50 Threads)</span>
            </button>

            <button
              onClick={handleReset}
              disabled={resetting}
              title="Reset stock to 10 for testing"
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                backgroundColor: '#1f2937',
                border: '1px solid #374151',
                color: '#9ca3af',
                padding: '0.45rem 0.65rem',
                borderRadius: '0.5rem',
                cursor: resetting ? 'not-allowed' : 'pointer'
              }}
            >
              <RotateCcw size={13} />
            </button>

          </div>

        </div>

      </div>

    </div>
  );
}
