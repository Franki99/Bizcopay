import nodemailer from 'nodemailer'

const DEV_MODE = !process.env.EMAIL_USER || !process.env.EMAIL_APP_PASSWORD

const transporter = DEV_MODE
  ? null
  : nodemailer.createTransport({
      host: 'smtp.gmail.com',
      port: 587,
      secure: false,
      auth: { user: process.env.EMAIL_USER, pass: process.env.EMAIL_APP_PASSWORD },
    })

const FROM_ADDRESS = process.env.EMAIL_FROM || process.env.EMAIL_USER

export async function sendOtpEmail(to: string, code: string, purpose: 'REGISTER' | 'RESET_PIN' | 'CHANGE_EMAIL') {
  const action = purpose === 'REGISTER' ? 'complete your registration'
    : purpose === 'RESET_PIN' ? 'reset your PIN'
    : 'confirm your new email address'
  const subject = purpose === 'REGISTER' ? 'Verify your Bizcopay account'
    : purpose === 'RESET_PIN' ? 'Reset your Bizcopay PIN'
    : 'Confirm your new Bizcopay email'

  console.log(`\n📧  OTP for ${to} [${purpose}]: ${code}  (expires in 10 min)\n`)

  if (DEV_MODE) return

  await transporter!.sendMail({
    from: `"Bizcopay" <${FROM_ADDRESS}>`,
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
