interface Props {
  page: number
  totalPages: number
  totalItems: number
  pageSize: number
  onChange: (page: number) => void
}

export default function Pagination({ page, totalPages, totalItems, pageSize, onChange }: Props) {
  if (totalPages <= 1) return null

  const from = (page - 1) * pageSize + 1
  const to   = Math.min(page * pageSize, totalItems)

  // Build visible page numbers: always show first, last, current ±1, with gaps
  function pages(): (number | '…')[] {
    const all = Array.from({ length: totalPages }, (_, i) => i + 1)
    if (totalPages <= 7) return all

    const near = new Set([1, totalPages, page - 1, page, page + 1].filter(n => n >= 1 && n <= totalPages))
    const sorted = [...near].sort((a, b) => a - b)

    const result: (number | '…')[] = []
    for (let i = 0; i < sorted.length; i++) {
      if (i > 0 && sorted[i] - sorted[i - 1] > 1) result.push('…')
      result.push(sorted[i])
    }
    return result
  }

  const btnBase  = 'min-w-[36px] h-9 flex items-center justify-center rounded-lg text-sm font-medium transition-colors'
  const btnPage  = `${btnBase} border`
  const btnActive = `${btnBase} bg-blue-600 text-white shadow-sm`
  const btnInactive = `${btnPage} border-gray-200 text-gray-600 hover:bg-gray-50`
  const btnDisabled = `${btnPage} border-gray-100 text-gray-300 cursor-not-allowed`

  return (
    <div className="flex items-center justify-between px-5 py-4 border-t border-gray-50">
      <p className="text-xs text-gray-400">
        Showing <span className="font-semibold text-gray-600">{from}–{to}</span> of <span className="font-semibold text-gray-600">{totalItems}</span>
      </p>

      <div className="flex items-center gap-1">
        {/* Prev */}
        <button
          onClick={() => onChange(page - 1)}
          disabled={page === 1}
          className={page === 1 ? btnDisabled : btnInactive}
          aria-label="Previous page"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
        </button>

        {/* Page numbers */}
        {pages().map((p, i) =>
          p === '…' ? (
            <span key={`gap-${i}`} className="min-w-[36px] h-9 flex items-center justify-center text-gray-300 text-sm">…</span>
          ) : (
            <button key={p} onClick={() => onChange(p as number)}
              className={p === page ? btnActive : btnInactive}>
              {p}
            </button>
          )
        )}

        {/* Next */}
        <button
          onClick={() => onChange(page + 1)}
          disabled={page === totalPages}
          className={page === totalPages ? btnDisabled : btnInactive}
          aria-label="Next page"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>
    </div>
  )
}
