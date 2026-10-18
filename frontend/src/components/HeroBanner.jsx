import React, { useState, useEffect } from 'react';
import { Flame, Clock, Cpu, ShieldAlert, Sparkles } from 'lucide-react';

export default function HeroBanner() {
  const [timeLeft, setTimeLeft] = useState({
    hours: 1,
    minutes: 42,
    seconds: 15
  });

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft(prev => {
        if (prev.seconds > 0) return { ...prev, seconds: prev.seconds - 1 };
        if (prev.minutes > 0) return { ...prev, minutes: prev.minutes - 1, seconds: 59 };
        if (prev.hours > 0) return { ...prev, hours: prev.hours - 1, minutes: 59, seconds: 59 };
        return prev;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const formatDigit = (num) => String(num).padStart(2, '0');

  return (
    <div style={{
      margin: '2rem 0',
      background: 'linear-gradient(135deg, rgba(30, 41, 59, 0.7), rgba(15, 23, 42, 0.9))',
      border: '1px solid rgba(245, 158, 11, 0.25)',
      borderRadius: '1rem',
      padding: '2rem',
      position: 'relative',
      overflow: 'hidden'
    }}>
      
      {/* Background Glow */}
      <div style={{
        position: 'absolute',
        top: '-50px',
        right: '-50px',
        width: '250px',
        height: '250px',
        background: 'radial-gradient(circle, rgba(245, 158, 11, 0.15) 0%, transparent 70%)',
        pointerEvents: 'none'
      }} />

      <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: '1.5rem' }}>
        
        {/* Left Column: Heading & Value Prop */}
        <div style={{ maxWidth: '640px' }}>
          
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.4rem',
            background: 'rgba(239, 68, 68, 0.15)',
            border: '1px solid rgba(239, 68, 68, 0.3)',
            color: '#f87171',
            padding: '0.25rem 0.65rem',
            borderRadius: '9999px',
            fontSize: '0.75rem',
            fontWeight: 700,
            textTransform: 'uppercase',
            letterSpacing: '0.05em',
            marginBottom: '0.85rem'
          }}>
            <Flame size={14} />
            <span>VIP Flash Sale Event • Extreme Traffic Mode</span>
          </div>

          <h1 style={{
            fontSize: '2.25rem',
            fontWeight: 800,
            lineHeight: 1.15,
            letterSpacing: '-0.03em',
            marginBottom: '0.75rem'
          }}>
            High-Concurrency <span style={{
              background: 'linear-gradient(to right, #f59e0b, #ef4444)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent'
            }}>Order Engine</span>
          </h1>

          <p style={{ color: '#94a3b8', fontSize: '0.95rem', lineHeight: 1.6 }}>
            Demonstrating how Spring Boot coordinates granular thread-safe inventory locks, prevents race conditions, and eliminates overselling under high concurrency.
          </p>

          {/* Architecture badges */}
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem', marginTop: '1.25rem' }}>
            <span style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.35rem',
              fontSize: '0.75rem',
              color: '#38bdf8',
              background: 'rgba(56, 189, 248, 0.1)',
              border: '1px solid rgba(56, 189, 248, 0.25)',
              padding: '0.25rem 0.6rem',
              borderRadius: '0.375rem',
              fontFamily: 'var(--font-mono)'
            }}>
              <Cpu size={13} />
              Atomic DB Decrement
            </span>

            <span style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.35rem',
              fontSize: '0.75rem',
              color: '#10b981',
              background: 'rgba(16, 185, 129, 0.1)',
              border: '1px solid rgba(16, 185, 129, 0.25)',
              padding: '0.25rem 0.6rem',
              borderRadius: '0.375rem',
              fontFamily: 'var(--font-mono)'
            }}>
              <ShieldAlert size={13} />
              0% Overselling Guaranteed
            </span>

            <span style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.35rem',
              fontSize: '0.75rem',
              color: '#f59e0b',
              background: 'rgba(245, 158, 11, 0.1)',
              border: '1px solid rgba(245, 158, 11, 0.25)',
              padding: '0.25rem 0.6rem',
              borderRadius: '0.375rem',
              fontFamily: 'var(--font-mono)'
            }}>
              <Sparkles size={13} />
              Idempotent Webhooks
            </span>
          </div>

        </div>

        {/* Right Column: Flash Timer Box */}
        <div style={{
          background: 'rgba(15, 23, 42, 0.85)',
          border: '1px solid #334155',
          borderRadius: '0.75rem',
          padding: '1.25rem 1.5rem',
          minWidth: '260px',
          textAlign: 'center',
          boxShadow: '0 8px 24px rgba(0, 0, 0, 0.3)'
        }}>
          
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '0.4rem',
            color: '#cbd5e1',
            fontSize: '0.8rem',
            fontWeight: 600,
            marginBottom: '0.75rem'
          }}>
            <Clock size={15} color="#f59e0b" />
            <span>FLASH SALE ENDS IN</span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}>
            
            <div style={{
              background: '#1e293b',
              padding: '0.5rem 0.75rem',
              borderRadius: '0.5rem',
              border: '1px solid #334155',
              minWidth: '55px'
            }}>
              <span style={{ fontSize: '1.5rem', fontWeight: 800, color: '#f8fafc', fontFamily: 'var(--font-mono)' }}>
                {formatDigit(timeLeft.hours)}
              </span>
              <span style={{ display: 'block', fontSize: '0.65rem', color: '#64748b', textTransform: 'uppercase', marginTop: '0.1rem' }}>Hours</span>
            </div>

            <span style={{ fontSize: '1.25rem', fontWeight: 700, color: '#f59e0b' }}>:</span>

            <div style={{
              background: '#1e293b',
              padding: '0.5rem 0.75rem',
              borderRadius: '0.5rem',
              border: '1px solid #334155',
              minWidth: '55px'
            }}>
              <span style={{ fontSize: '1.5rem', fontWeight: 800, color: '#f8fafc', fontFamily: 'var(--font-mono)' }}>
                {formatDigit(timeLeft.minutes)}
              </span>
              <span style={{ display: 'block', fontSize: '0.65rem', color: '#64748b', textTransform: 'uppercase', marginTop: '0.1rem' }}>Mins</span>
            </div>

            <span style={{ fontSize: '1.25rem', fontWeight: 700, color: '#f59e0b' }}>:</span>

            <div style={{
              background: '#1e293b',
              padding: '0.5rem 0.75rem',
              borderRadius: '0.5rem',
              border: '1px solid #334155',
              minWidth: '55px'
            }}>
              <span style={{ fontSize: '1.5rem', fontWeight: 800, color: '#ef4444', fontFamily: 'var(--font-mono)' }}>
                {formatDigit(timeLeft.seconds)}
              </span>
              <span style={{ display: 'block', fontSize: '0.65rem', color: '#64748b', textTransform: 'uppercase', marginTop: '0.1rem' }}>Secs</span>
            </div>

          </div>

          <p style={{ fontSize: '0.7rem', color: '#64748b', marginTop: '0.75rem' }}>
            Strict limit: 1 unit per customer
          </p>

        </div>

      </div>

    </div>
  );
}
