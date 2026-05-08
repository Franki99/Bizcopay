import nodemailer from 'nodemailer'

const DEV_MODE = !process.env.EMAIL_USER || !process.env.EMAIL_PASS

const transporter = DEV_MODE
  ? null
  : nodemailer.createTransport({
      service: 'gmail',
      auth: { user: process.env.EMAIL_USER, pass: process.env.EMAIL_PASS },
    })

export async function sendOtpEmail(to: string, code: string, purpose: 'REGISTER' | 'RESET_PIN') {
  const action = purpose === 'REGISTER' ? 'complete your registration' : 'reset your PIN'
  const subject = purpose === 'REGISTER' ? 'Verify your Bizcopay account' : 'Reset your Bizcopay PIN'

  if (DEV_MODE) {
    console.log(`\n📧  OTP for ${to} [${purpose}]: ${code}  (expires in 10 min)\n`)
    return
  }

  await transporter!.sendMail({
    from: `"Bizcopay" <${process.env.EMAIL_USER}>`,
    to,
    subject,
    html: `
      <div style="font-family:sans-serif;max-width:420px;margin:0 auto;padding:24px">
        <h2 style="color:#1a1a2e">Your verification code</h2>
        <p>Use this code to ${action}:</p>
        <div style="background:#f4f4f5;border-radius:8px;padding:16px;text-align:center;margin:16px 0">
          <span style="font-size:32px;font-weight:bold;letter-spacing:8px;color:#1a1a2e">${code}</span>
        </div>
        <p style="color:#666;font-size:14px">This code expires in 10 minutes. Do not share it with anyone.</p>
      </div>`,
  })
}
