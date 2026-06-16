'use client'

import { useEffect, useState } from 'react'
import { useRouter, useParams } from 'next/navigation'
import Link from 'next/link'
import { api, UserDetail, Transaction, TopUpRequest } from '@/lib/api'
import PageShell from '@/components/PageShell'
import Pagination from '@/components/Pagination'

// ─── Colours ─────────────────────────────────────────────────────────────────
const roleBadge: Record<string, string> = {
  PAYER:    'bg-blue-100 text-blue-700',
  MERCHANT: 'bg-violet-100 text-violet-700',
  ADMIN:    'bg-orange-100 text-orange-700',
}
const roleAvatar: Record<string, string> = {
  PAYER:    'bg-blue-600',
  MERCHANT: 'bg-violet-600',
  ADMIN:    'bg-orange-500',
}
const txStatusColor: Record<string, string> = {
  APPROVED:     'bg-green-100 text-green-700',
  FAILED:       'bg-red-100 text-red-700',
  PENDING:      'bg-yellow-100 text-yellow-700',
  AWAITING_PIN: 'bg-blue-100 text-blue-700',
  EXPIRED:      'bg-gray-100 text-gray-500',
}
const txStatusDot: Record<string, string> = {
  APPROVED:     'bg-green-500',
  FAILED:       'bg-red-500',
  PENDING:      'bg-yellow-400',
  AWAITING_PIN: 'bg-blue-500',
  EXPIRED:      'bg-gray-400',
}
const topupColor: Record<string, string> = {
  PENDING:  'bg-yellow-100 text-yellow-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
}

const TX_PAGE_SIZE    = 8
const TOPUP_PAGE_SIZE = 8

// ─── PDF statement (matches mobile StatementHelper style) ────────────────────
async function downloadStatementPDF(
  user: UserDetail,
  transactions: Transaction[],
  topUps: TopUpRequest[],
) {
  // Dynamic import — jsPDF is browser-only
  const { default: jsPDF } = await import('jspdf')
  const { default: autoTable } = await import('jspdf-autotable')

  const doc    = new jsPDF({ orientation: 'portrait', unit: 'pt', format: 'a4' })
  const PAGE_W = doc.internal.pageSize.getWidth()
  const PAGE_H = doc.internal.pageSize.getHeight()
  const M      = 48
  const HDR_H  = 103   // header zone height (matches mobile)

  const isPayer = user.role === 'PAYER'
  const title   = isPayer ? 'Transaction Statement' : 'Sales Statement'

  const approvedTxs    = transactions.filter(t => t.status === 'APPROVED')
  const totalVolume    = approvedTxs.reduce((s, t) => s + parseFloat(t.amount), 0)
  const totalCredited  = topUps.filter(r => r.status === 'APPROVED').reduce((s, r) => s + parseFloat(r.amount), 0)

  // Keep autoTable content below the header zone on every page
  const tableMargin = { top: M + HDR_H + 8, left: M, right: M, bottom: M + 24 }

  // ── Page-1 summary (drawn before table so autoTable startY can follow) ───────
  const sumY = M + HDR_H + 18
  doc.setFont('helvetica', 'bold')
  doc.setFontSize(13)
  doc.setTextColor(13, 27, 54)
  doc.text(`Total: RWF ${totalVolume.toLocaleString('en-US', { minimumFractionDigits: 2 })}`, M, sumY)

  doc.setFont('helvetica', 'normal')
  doc.setFontSize(10)
  doc.setTextColor(90, 106, 133)
  doc.text(`${approvedTxs.length} approved  •  ${transactions.length} total`, M, sumY + 16)

  let txStartY = sumY + 38
  if (isPayer && totalCredited > 0) {
    doc.setTextColor(29, 185, 84)
    doc.text(`Total credits: +RWF ${totalCredited.toLocaleString('en-US', { minimumFractionDigits: 2 })}`, M, sumY + 32)
    txStartY += 20
  }

  // ── Transactions table ────────────────────────────────────────────────────────
  const txHead = [['Date', isPayer ? 'Merchant' : 'Customer', 'Amount (RWF)', 'Status', 'Risk']]
  const txBody = transactions.map(tx => [
    new Date(tx.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }),
    isPayer ? tx.merchant.name : (tx.payer?.name ?? '—'),
    (isPayer ? '− ' : '+ ') + parseFloat(tx.amount).toLocaleString('en-US', { minimumFractionDigits: 2 }),
    tx.status,
    tx.fraudLog?.riskScore ?? '—',
  ])

  autoTable(doc, {
    startY:       txStartY,
    margin:       tableMargin,
    head:         txHead,
    body:         txBody,
    headStyles:   { fillColor: [238, 242, 248], textColor: [13, 27, 54], fontStyle: 'bold', fontSize: 9, lineWidth: 0 },
    bodyStyles:   { fontSize: 9, textColor: [13, 27, 54], lineWidth: 0 },
    alternateRowStyles: { fillColor: [248, 249, 252] },
    columnStyles: {
      0: { cellWidth: 72 },
      1: { cellWidth: 'auto' },
      2: { cellWidth: 95, halign: 'right' },
      3: { cellWidth: 80 },
      4: { cellWidth: 46 },
    },
    didParseCell: data => {
      if (data.section !== 'body') return
      const row = data.row.raw as string[]
      if (data.column.index === 3) {
        if (row[3] === 'APPROVED') data.cell.styles.textColor = [29, 185, 84]
        else if (row[3] === 'FAILED') data.cell.styles.textColor = [229, 57, 53]
      }
      if (data.column.index === 2) {
        const ok = row[3] === 'APPROVED'
        data.cell.styles.textColor = ok ? (isPayer ? [229, 57, 53] : [29, 185, 84]) : [150, 150, 150]
        data.cell.styles.fontStyle = 'bold'
      }
    },
  })

  // ── Top-up table (payers only) ────────────────────────────────────────────────
  if (isPayer && topUps.length > 0) {
    const txFinalY: number = (doc as any).lastAutoTable?.finalY ?? txStartY + 60
    let sectionY = txFinalY + 24
    if (sectionY > PAGE_H - tableMargin.bottom - 60) {
      doc.addPage()
      sectionY = tableMargin.top + 8
    }

    doc.setFont('helvetica', 'bold')
    doc.setFontSize(10)
    doc.setTextColor(13, 27, 54)
    doc.text('CREDITS / TOP-UP REQUESTS', M, sectionY)

    const tuBody = topUps.map(r => [
      new Date(r.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }),
      parseFloat(r.amount).toLocaleString('en-US', { minimumFractionDigits: 2 }),
      r.note ?? '—',
      r.status,
    ])

    autoTable(doc, {
      startY:       sectionY + 12,
      margin:       tableMargin,
      head:         [['Date', 'Amount (RWF)', 'Note', 'Status']],
      body:         tuBody,
      headStyles:   { fillColor: [238, 242, 248], textColor: [13, 27, 54], fontStyle: 'bold', fontSize: 9, lineWidth: 0 },
      bodyStyles:   { fontSize: 9, textColor: [13, 27, 54], lineWidth: 0 },
      alternateRowStyles: { fillColor: [248, 249, 252] },
      columnStyles: {
        0: { cellWidth: 90 },
        1: { cellWidth: 100, halign: 'right' },
        2: { cellWidth: 'auto' },
        3: { cellWidth: 70 },
      },
      didParseCell: data => {
        if (data.section !== 'body') return
        const row = data.row.raw as string[]
        if (data.column.index === 3) {
          if (row[3] === 'APPROVED') data.cell.styles.textColor = [29, 185, 84]
          else if (row[3] === 'REJECTED') data.cell.styles.textColor = [229, 57, 53]
        }
        if (data.column.index === 1) {
          data.cell.styles.textColor = [29, 185, 84]
          data.cell.styles.fontStyle = 'bold'
        }
      },
    })
  }

  // ── Second pass: draw header decoration + page numbers on every page ──────────
  // (drawn last so they sit on top of any table overflow into the margin zone)
  const totalPages = doc.getNumberOfPages()
  const genDate    = new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
  const maskedId   = user.id.length > 8 ? `${user.id.slice(0, 4)}…${user.id.slice(-4)}` : user.id

  for (let p = 1; p <= totalPages; p++) {
    doc.setPage(p)

    // Blue "Bizcopay" wordmark
    doc.setFont('helvetica', 'bold')
    doc.setFontSize(22)
    doc.setTextColor(26, 75, 138)
    doc.text('Bizcopay', M, M + 10)

    // Statement type
    doc.setFontSize(12)
    doc.setTextColor(90, 106, 133)
    doc.text(title, M, M + 28)

    // User name
    doc.setFont('helvetica', 'normal')
    doc.setFontSize(11)
    doc.setTextColor(13, 27, 54)
    doc.text(user.name, M, M + 48)

    // Email + account ID
    doc.setFontSize(10)
    doc.setTextColor(90, 106, 133)
    doc.text(user.email, M, M + 62)
    doc.text(`Account ID: ${maskedId}`, M, M + 76)

    // Generated date
    doc.setTextColor(154, 170, 191)
    doc.text(`Generated: ${genDate}`, M, M + 90)

    // Divider line
    doc.setDrawColor(221, 227, 238)
    doc.setLineWidth(0.5)
    doc.line(M, M + HDR_H, PAGE_W - M, M + HDR_H)

    // Footer: page number + brand
    doc.setFontSize(9)
    doc.setFont('helvetica', 'normal')
    doc.setTextColor(90, 106, 133)
    doc.text(`Page ${p} of ${totalPages}`, PAGE_W - M, PAGE_H - 24, { align: 'right' })
    doc.text('Bizcopay Financial Statement', M, PAGE_H - 24)
  }

  const slug = user.name.replace(/\s+/g, '-').toLowerCase()
  doc.save(`statement-${slug}-${new Date().toISOString().slice(0, 10)}.pdf`)
}

// ─── Page ─────────────────────────────────────────────────────────────────────
export default function UserDetailPage() {
  const router = useRouter()
  const { id } = useParams<{ id: string }>()

  const [user,         setUser]         = useState<UserDetail | null>(null)
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [topUps,       setTopUps]       = useState<TopUpRequest[]>([])
  const [loading,      setLoading]      = useState(true)
  const [generating,   setGenerating]   = useState(false)
  const [txFilter,     setTxFilter]     = useState<'ALL' | 'APPROVED' | 'FAILED'>('ALL')
  const [txPage,       setTxPage]       = useState(1)
  const [topUpPage,    setTopUpPage]    = useState(1)

  useEffect(() => {
    if (!id) return
    Promise.all([
      api.getUserById(id),
      api.getUserTransactions(id),
      api.getUserTopUpRequests(id),
    ])
      .then(([u, txs, ups]) => { setUser(u); setTransactions(txs); setTopUps(ups) })
      .catch(() => router.push('/login'))
      .finally(() => setLoading(false))
  }, [id])

  function applyTxFilter(f: 'ALL' | 'APPROVED' | 'FAILED') { setTxFilter(f); setTxPage(1) }

  const filteredTxs  = transactions.filter(tx => txFilter === 'ALL' || tx.status === txFilter)
  const txTotalPages = Math.max(1, Math.ceil(filteredTxs.length / TX_PAGE_SIZE))
  const txSafePage   = Math.min(txPage, txTotalPages)
  const paginatedTxs = filteredTxs.slice((txSafePage - 1) * TX_PAGE_SIZE, txSafePage * TX_PAGE_SIZE)

  const tuTotalPages = Math.max(1, Math.ceil(topUps.length / TOPUP_PAGE_SIZE))
  const tuSafePage   = Math.min(topUpPage, tuTotalPages)
  const paginatedTus = topUps.slice((tuSafePage - 1) * TOPUP_PAGE_SIZE, tuSafePage * TOPUP_PAGE_SIZE)

  const approvedTxs         = transactions.filter(t => t.status === 'APPROVED')
  const totalVolume          = approvedTxs.reduce((s, t) => s + parseFloat(t.amount), 0)
  const totalTopUpCredited   = topUps.filter(r => r.status === 'APPROVED').reduce((s, r) => s + parseFloat(r.amount), 0)

  async function handleDownload() {
    if (!user) return
    setGenerating(true)
    try { await downloadStatementPDF(user, transactions, topUps) }
    finally { setGenerating(false) }
  }

  if (loading) return (
    <PageShell>
      <div className="flex h-64 items-center justify-center">
        <div className="w-8 h-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
      </div>
    </PageShell>
  )

  if (!user) return (
    <PageShell>
      <div className="flex h-64 items-center justify-center text-gray-400">User not found</div>
    </PageShell>
  )

  const isPayer    = user.role === 'PAYER'
  const isMerchant = user.role === 'MERCHANT'

  return (
    <PageShell>
      {/* ── Back + header ──────────────────────────────────────────────────── */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <Link href="/users"
            className="inline-flex items-center gap-1 px-3 py-2 text-sm text-gray-500 border border-gray-200 bg-white rounded-xl hover:bg-gray-50 transition-colors">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
            </svg>
            Users
          </Link>
          <span className="text-gray-300">/</span>
          <h2 className="text-xl font-bold text-gray-900">{user.name}</h2>
        </div>
        <button onClick={handleDownload} disabled={generating}
          className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded-xl hover:bg-blue-700 disabled:opacity-60 transition-colors shadow-sm">
          {generating ? (
            <div className="w-4 h-4 border-2 border-blue-200 border-t-white rounded-full animate-spin" />
          ) : (
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
          )}
          {generating ? 'Generating…' : 'Download Statement'}
        </button>
      </div>

      {/* ── Profile card ───────────────────────────────────────────────────── */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 mb-6">
        <div className="flex flex-wrap items-start gap-5">
          <div className={`w-16 h-16 rounded-2xl ${roleAvatar[user.role] ?? 'bg-gray-400'} flex items-center justify-center text-white text-2xl font-bold shrink-0`}>
            {user.name[0].toUpperCase()}
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex flex-wrap items-center gap-2 mb-1">
              <h3 className="text-xl font-bold text-gray-900">{user.name}</h3>
              <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${roleBadge[user.role] ?? 'bg-gray-100 text-gray-600'}`}>
                {user.role}
              </span>
              <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold ${user.isActive ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                <span className={`w-1.5 h-1.5 rounded-full ${user.isActive ? 'bg-green-500' : 'bg-red-500'}`} />
                {user.isActive ? 'Active' : 'Inactive'}
              </span>
            </div>
            <p className="text-sm text-gray-500">{user.email}</p>
            <p className="text-xs text-gray-400 mt-1">
              Joined {new Date(user.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'long', year: 'numeric' })}
            </p>
          </div>
          <div className="flex flex-wrap gap-4">
            {user.wallet && (
              <div className="text-right">
                <p className="text-xs text-gray-400 uppercase tracking-wide">Wallet Balance</p>
                <p className="text-2xl font-bold text-gray-900">
                  {user.wallet.currency} {parseFloat(user.wallet.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </p>
              </div>
            )}
            <div className="text-right">
              <p className="text-xs text-gray-400 uppercase tracking-wide">Transactions</p>
              <p className="text-2xl font-bold text-gray-900">{transactions.length}</p>
            </div>
            {isPayer && (
              <>
                <div className="text-right">
                  <p className="text-xs text-gray-400 uppercase tracking-wide">Total Spent</p>
                  <p className="text-2xl font-bold text-gray-900">RWF {totalVolume.toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
                </div>
                {user.nfcTokens.length > 0 && (
                  <div className="text-right">
                    <p className="text-xs text-gray-400 uppercase tracking-wide">NFC Devices</p>
                    <p className="text-2xl font-bold text-gray-900">{user.nfcTokens.length}</p>
                  </div>
                )}
              </>
            )}
            {isMerchant && (
              <div className="text-right">
                <p className="text-xs text-gray-400 uppercase tracking-wide">Revenue Received</p>
                <p className="text-2xl font-bold text-gray-900">RWF {totalVolume.toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ── Transactions table ─────────────────────────────────────────────── */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden mb-6">
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-50">
          <h4 className="font-semibold text-gray-900">
            {isPayer ? 'Payment History' : 'Transaction History'}
            <span className="ml-2 text-sm font-normal text-gray-400">({filteredTxs.length})</span>
          </h4>
          <div className="flex gap-1.5">
            {(['ALL', 'APPROVED', 'FAILED'] as const).map(f => (
              <button key={f} onClick={() => applyTxFilter(f)}
                className={`px-3 py-1.5 text-xs rounded-lg font-semibold transition-colors ${
                  txFilter === f ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}>
                {f === 'ALL'
                  ? `All (${transactions.length})`
                  : f === 'APPROVED'
                    ? `Approved (${approvedTxs.length})`
                    : `Failed (${transactions.filter(t => t.status === 'FAILED').length})`}
              </button>
            ))}
          </div>
        </div>

        {filteredTxs.length === 0 ? (
          <div className="py-16 text-center text-sm text-gray-400">No transactions found</div>
        ) : (
          <>
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-400 text-xs uppercase">
                <tr>
                  {['Date', isPayer ? 'Merchant' : 'Payer', 'Amount', 'Status', 'Risk'].map(h =>
                    <th key={h} className="px-5 py-3 text-left font-medium tracking-wide">{h}</th>
                  )}
                </tr>
              </thead>
              <tbody>
                {paginatedTxs.map((tx, i) => (
                  <tr key={tx.id} className={`hover:bg-gray-50 transition-colors ${i !== 0 ? 'border-t border-gray-50' : ''}`}>
                    <td className="px-5 py-3.5 text-xs text-gray-400">
                      {new Date(tx.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
                      <br /><span className="text-gray-300">{new Date(tx.createdAt).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}</span>
                    </td>
                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-2">
                        <div className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold text-white shrink-0 ${isPayer ? 'bg-violet-500' : 'bg-blue-500'}`}>
                          {(isPayer ? tx.merchant.name : (tx.payer?.name ?? '?'))[0].toUpperCase()}
                        </div>
                        <div className="min-w-0">
                          <p className="font-medium text-gray-900 truncate">{isPayer ? tx.merchant.name : (tx.payer?.name ?? '—')}</p>
                          <p className="text-xs text-gray-400 truncate">{isPayer ? tx.merchant.email : (tx.payer?.email ?? '')}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`font-bold ${tx.status === 'APPROVED' ? (isPayer ? 'text-red-600' : 'text-green-600') : 'text-gray-400'}`}>
                        {isPayer ? '−' : '+'} RWF {parseFloat(tx.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${txStatusColor[tx.status] ?? 'bg-gray-100 text-gray-600'}`}>
                        <span className={`w-1.5 h-1.5 rounded-full ${txStatusDot[tx.status] ?? 'bg-gray-400'}`} />
                        {tx.status}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      {tx.fraudLog ? (
                        <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-semibold ${tx.fraudLog.riskScore === 'HIGH' ? 'bg-red-100 text-red-700' : 'bg-yellow-100 text-yellow-700'}`}>
                          ⚠ {tx.fraudLog.riskScore}
                        </span>
                      ) : (
                        <span className="text-xs text-gray-300">—</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <Pagination
              page={txSafePage}
              totalPages={txTotalPages}
              totalItems={filteredTxs.length}
              pageSize={TX_PAGE_SIZE}
              onChange={setTxPage}
            />
          </>
        )}
      </div>

      {/* ── Top-Up Requests (payers only) ──────────────────────────────────── */}
      {isPayer && (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="flex items-center justify-between px-5 py-4 border-b border-gray-50">
            <h4 className="font-semibold text-gray-900">
              Top-Up Requests
              <span className="ml-2 text-sm font-normal text-gray-400">({topUps.length})</span>
            </h4>
            {topUps.length > 0 && (
              <span className="text-xs text-gray-400">
                Total credited: <span className="font-semibold text-green-600">RWF {totalTopUpCredited.toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
              </span>
            )}
          </div>

          {topUps.length === 0 ? (
            <div className="py-16 text-center text-sm text-gray-400">No top-up requests</div>
          ) : (
            <>
              <table className="w-full text-sm">
                <thead className="bg-gray-50 text-gray-400 text-xs uppercase">
                  <tr>
                    {['Date', 'Amount', 'Note', 'Status'].map(h =>
                      <th key={h} className="px-5 py-3 text-left font-medium tracking-wide">{h}</th>
                    )}
                  </tr>
                </thead>
                <tbody>
                  {paginatedTus.map((req, i) => (
                    <tr key={req.id} className={`hover:bg-gray-50 transition-colors ${i !== 0 ? 'border-t border-gray-50' : ''}`}>
                      <td className="px-5 py-3.5 text-xs text-gray-400">
                        {new Date(req.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
                        <br /><span className="text-gray-300">{new Date(req.createdAt).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}</span>
                      </td>
                      <td className="px-5 py-3.5 font-bold text-gray-900">
                        RWF {parseFloat(req.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                      </td>
                      <td className="px-5 py-3.5 text-xs text-gray-400 max-w-48 truncate">
                        {req.note ?? <span className="text-gray-200">—</span>}
                      </td>
                      <td className="px-5 py-3.5">
                        <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${topupColor[req.status] ?? 'bg-gray-100 text-gray-600'}`}>
                          {req.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <Pagination
                page={tuSafePage}
                totalPages={tuTotalPages}
                totalItems={topUps.length}
                pageSize={TOPUP_PAGE_SIZE}
                onChange={setTopUpPage}
              />
            </>
          )}
        </div>
      )}
    </PageShell>
  )
}
