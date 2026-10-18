import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import HeroBanner from './components/HeroBanner';
import ProductCard from './components/ProductCard';
import StressTestModal from './components/StressTestModal';
import RecentOrders from './components/RecentOrders';
import { getProducts, getRecentOrders, placeFlashBuyOrder, resetProductStock } from './services/api';
import confetti from 'canvas-confetti';
import { AlertCircle, CheckCircle2, ShieldCheck, Zap } from 'lucide-react';

export default function App() {
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [buyingId, setBuyingId] = useState(null);
  const [stressTestProduct, setStressTestProduct] = useState(null);
  const [toast, setToast] = useState(null);

  const showToast = (type, message) => {
    setToast({ type, message });
    setTimeout(() => {
      setToast(null);
    }, 4000);
  };

  const loadData = async (isManual = false) => {
    if (isManual) setRefreshing(true);
    try {
      const [productList, orderList] = await Promise.all([
        getProducts(),
        getRecentOrders()
      ]);
      setProducts(productList);
      setOrders(orderList);
    } catch (err) {
      console.error('Failed to load FlashFlow data:', err);
      // Fallback mock data if backend is not yet started, so frontend still looks gorgeous!
      if (products.length === 0) {
        setProducts([
          {
            id: 1,
            title: "Sony PlayStation 5 Pro (Limited Edition)",
            description: "Next-gen gaming console with 2TB high-speed SSD and AI-enhanced PSSR upscaling.",
            price: 699.99,
            stockQuantity: 5,
            initialStock: 5,
            percentageLeft: 100,
            imageUrl: "https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=600&auto=format&fit=crop&q=80",
            category: "Gaming"
          },
          {
            id: 2,
            title: "Apple iPhone 16 Pro Max (Titanium Desert)",
            description: "A18 Pro Bionic processor, 48MP Fusion camera, and revolutionary battery life.",
            price: 1199.99,
            stockQuantity: 10,
            initialStock: 10,
            percentageLeft: 100,
            imageUrl: "https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=600&auto=format&fit=crop&q=80",
            category: "Smartphones"
          },
          {
            id: 3,
            title: "NVIDIA GeForce RTX 5090 OC 32GB",
            description: "Ultra-enthusiast Blackwell architecture GPU for 8K gaming and local LLM AI inference.",
            price: 1999.99,
            stockQuantity: 3,
            initialStock: 3,
            percentageLeft: 100,
            imageUrl: "https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?w=600&auto=format&fit=crop&q=80",
            category: "Components"
          },
          {
            id: 4,
            title: "Keychron Q1 Pro Custom Mechanical Keyboard",
            description: "Full aluminum CNC body, hot-swappable switches, wireless Bluetooth 5.1.",
            price: 199.99,
            stockQuantity: 15,
            initialStock: 15,
            percentageLeft: 100,
            imageUrl: "https://images.unsplash.com/photo-1595225476474-87563907a212?w=600&auto=format&fit=crop&q=80",
            category: "Accessories"
          }
        ]);
      }
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(() => {
      loadData(false);
    }, 4000); // Polling every 4 seconds for live stock changes
    return () => clearInterval(interval);
  }, []);

  const handleFlashBuy = async (productId) => {
    setBuyingId(productId);
    try {
      const response = await placeFlashBuyOrder(productId);
      
      // Trigger celebration confetti on success!
      confetti({
        particleCount: 80,
        spread: 60,
        origin: { y: 0.6 }
      });

      showToast('success', `🎉 Order ${response.orderNumber} confirmed! Lock acquired & stock decremented.`);
      await loadData();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to complete flash buy';
      showToast('error', msg);
    } finally {
      setBuyingId(null);
    }
  };

  const handleResetStock = async (productId, stock) => {
    try {
      await resetProductStock(productId, stock);
      showToast('success', 'Product stock successfully reset to ' + stock);
      await loadData();
    } catch (err) {
      showToast('error', 'Failed to reset stock: ' + err.message);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      
      <Navbar onRefresh={() => loadData(true)} isRefreshing={refreshing} />

      <main className="container" style={{ flexGrow: 1, paddingBottom: '4rem' }}>
        
        {/* Toast Alert */}
        {toast && (
          <div style={{
            position: 'fixed',
            bottom: '24px',
            right: '24px',
            zIndex: 150,
            display: 'flex',
            alignItems: 'center',
            gap: '0.6rem',
            backgroundColor: toast.type === 'success' ? '#064e3b' : '#7f1d1d',
            border: `1px solid ${toast.type === 'success' ? '#10b981' : '#ef4444'}`,
            color: '#f8fafc',
            padding: '0.85rem 1.25rem',
            borderRadius: '0.625rem',
            boxShadow: '0 10px 25px rgba(0,0,0,0.4)',
            maxWidth: '420px',
            fontSize: '0.875rem',
            fontWeight: 600,
            animation: 'slideUp 0.25s ease'
          }}>
            {toast.type === 'success' ? <CheckCircle2 size={18} color="#10b981" /> : <AlertCircle size={18} color="#ef4444" />}
            <span>{toast.message}</span>
          </div>
        )}

        <HeroBanner />

        {/* Catalog Section Header */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
          <div>
            <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#f8fafc' }}>
              Flash-Sale Inventory Catalog
            </h2>
            <p style={{ fontSize: '0.85rem', color: '#64748b' }}>
              Stock quantities decrement atomically in the database with granular concurrency locks
            </p>
          </div>
        </div>

        {/* Product Cards Grid */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(270px, 1fr))',
          gap: '1.5rem'
        }}>
          {products.map(p => (
            <ProductCard
              key={p.id}
              product={p}
              onFlashBuy={handleFlashBuy}
              onOpenStressTest={(prod) => setStressTestProduct(prod)}
              onResetStock={handleResetStock}
              isBuying={buyingId === p.id}
            />
          ))}
        </div>

        {/* Live Orders Audit Trail */}
        <RecentOrders orders={orders} />

      </main>

      {/* Concurrency Stress Test Modal */}
      {stressTestProduct && (
        <StressTestModal
          product={stressTestProduct}
          onClose={() => setStressTestProduct(null)}
          onTestComplete={() => loadData()}
        />
      )}

      {/* Footer */}
      <footer style={{
        borderTop: '1px solid #1e293b',
        padding: '1.5rem 0',
        backgroundColor: '#0a0e17',
        textAlign: 'center',
        color: '#64748b',
        fontSize: '0.8rem'
      }}>
        <div className="container">
          FlashFlow &copy; 2026 • Full-Stack Concurrency Engineering with Spring Boot &amp; React
        </div>
      </footer>

      <style>{`
        @keyframes slideUp {
          from { transform: translateY(20px); opacity: 0; }
          to { transform: translateY(0); opacity: 1; }
        }
      `}</style>
    </div>
  );
}
