# CHAPTER 2

# ANALYSIS OF THE EXISTING SYSTEM

## Introduction

This chapter examines the existing payment landscape relevant to the Bizcopay project from two perspectives. The first is the Bizcotap organisation, whose NFC-enabled accessories form the hardware foundation of the proposed system. Understanding Bizcotap's background, product catalogue, and current positioning clarifies what hardware already exists in consumers' hands and what payment capability has not yet been unlocked. The second perspective is the merchant payment environment in Rwanda, where USSD-based mobile money, QR code payments, and bank-issued point-of-sale terminals each represent an existing approach to the same problem that Bizcopay addresses. Analysing each method — its flow, strengths, and limitations — reveals the specific gaps that justify a new solution. The chapter also reviews related work in the NFC contactless payment domain, drawing on commercial implementations and academic research that inform the design decisions detailed in Chapter Three.

## Bizcotap: The Case Study Organisation

**Historical Background**

Bizcotap is a technology company specialising in NFC (Near Field Communication) and QR-code-enabled accessories designed to make physical-to-digital interactions instantaneous. The company was built around a single insight: that smartphones capable of reading NFC chips are already in the pockets of millions of consumers, yet the everyday objects that people carry — a ring, a business card, a wristband — have not been made to take advantage of that capability. By embedding NFC chips into accessories that consumers wear and carry naturally, Bizcotap allows a tap of a phone against an accessory to trigger a digital action without requiring the user to open an application, type a URL, or carry additional hardware. The company tagline — *Seamless Connection at your Finger Tips* — reflects this philosophy of reducing friction between the physical and digital worlds to near zero.

Bizcotap's product line addresses several interaction scenarios. In professional networking, a smart business card allows a person to share their contact details, portfolio, or social media profile instantly by having another person tap their phone against the card — eliminating the need to exchange physical cards that must later be manually entered into a contacts application. In consumer identity, an NFC ring or wristband serves as a personal digital identity token that can trigger a pre-programmed action on any compatible smartphone. In events and access control, NFC stretch bands allow organisers to issue attendees a wearable credential that can be verified instantly at entry points using any NFC-capable phone. Across all these applications, the same hardware — a passive NFC chip embedded in an accessory — performs the identification function, and the server infrastructure behind it determines what action is taken.

**Mission**

Bizcotap's mission is to make physical-to-digital interactions seamless by embedding NFC and QR connectivity into everyday accessories, enabling people and organisations to share, connect, and engage in an instant — without friction, without installation, and without the need for proprietary hardware beyond the accessory itself.

**Vision**

Bizcotap envisions a world where every personal accessory is a gateway to digital services, where tapping a ring, card, or wristband against a smartphone is as natural and universal as a handshake — and where that single tap can trigger any digital interaction, from sharing a contact to completing a payment.

**Products and Technology**

Bizcotap's product catalogue is built around four accessory categories, each embedding the same core NFC technology in a different physical form factor designed for a specific user context.

Smart NFC rings are worn on the finger as jewellery and contain a passive NFC chip within the band. They are produced in multiple sizes and materials — typically stainless steel or resin — and are designed for everyday wear. The ring's NFC chip is programmed with a URL or identifier that is resolved when a compatible smartphone is brought within a few centimetres of the ring's surface.

Smart business cards are produced in PVC or metal, combining a professionally printed surface with an embedded NFC chip and a printed QR code. They function as a digital-physical hybrid: a person receiving the card can either tap it against an NFC-capable phone or scan the QR code with a camera to access the same digital destination. This dual-mode design ensures compatibility with all smartphones, including those on which NFC reading requires an application.

Silicone stretch bands are flexible wristbands carrying an NFC chip, similar in form to the wristbands used at events and festivals. They are low-cost, waterproof, and can be manufactured in bulk, making them well-suited for distribution at events, sports facilities, and loyalty programmes. The embedded NFC chip performs the same identification function as the ring or card but in a form factor associated with temporary or activity-specific use.

Phone cases with adhesive or integrated NFC tags allow users to retrofit NFC capability onto a device by attaching an NFC-enabled case or sticker, extending the concept of a smart accessory to the device users already hold constantly. This form factor requires no change in the user's existing habits beyond the attachment of the case.

All four accessory categories are built around passive NFC chips operating at 13.56 MHz in compliance with the ISO/IEC 14443 and ISO/IEC 18092 international standards. The chips store an NFC Data Exchange Format (NDEF) record containing a URL or a unique identifier. They require no battery and draw their operating power entirely from the electromagnetic field of the reading device. Each chip contains a unique identifier (UID) assigned at manufacture, which serves as an immutable hardware address distinct from the NDEF content.

Bizcotap accessories are compatible with Apple devices from iPhone 7 onwards, Samsung Galaxy devices from 2014 onwards, and all Android devices running Android 10 and above with NFC hardware. This broad compatibility ensures that the accessories function as payment credential tokens across the overwhelming majority of smartphones currently in use, without requiring any additional hardware investment beyond the accessory itself.

Bizcotap's current commercial positioning focuses on identity and networking use cases. The NFC hardware in their accessories is fully capable, however, of functioning as a payment token when backed by a secure server infrastructure — a capability that Bizcopay demonstrates and extends.

## Existing Payment Methods in Rwanda

Rwanda's merchant payment landscape comprises three principal methods in active use: USSD-based mobile money merchant payments, QR code payment methods, and bank-issued point-of-sale terminals. Each method addresses the merchant payment problem with a different set of trade-offs.

**USSD-Based Mobile Money Merchant Payments**

Mobile money services have achieved wide adoption in Rwanda, enabling millions of users to store and transfer value digitally using a basic mobile phone without requiring an internet connection. Merchant payment via USSD (Unstructured Supplementary Service Data) works by having the payer dial a shortcode — such as those provided by MTN Mobile Money or Airtel Money — and navigate a hierarchical menu sequence to specify the merchant, the amount, and confirm the transaction with a PIN. The session operates over GSM signalling channels and imposes a timeout of approximately 180 seconds on the entire sequence.

The payer experience in a typical USSD merchant payment requires between four and seven distinct interactions: dialling the shortcode, selecting the payment option from a menu, entering the merchant's payment code, entering the amount, reviewing a confirmation screen, and entering a PIN to authorise. Any typographical error in the merchant code or the amount requires the payer to abandon the session and restart from the beginning. The process takes between thirty seconds and two minutes under normal conditions, and longer when network congestion increases menu response times. Transaction confirmation reaches the payer as an SMS notification that may arrive seconds or minutes after the transaction completes, and the merchant relies on their own SMS notification to confirm receipt — meaning both parties are left uncertain about the outcome during the interval between completion and notification.

**QR Code Payment Methods**

QR code payment methods have been introduced by mobile money operators and banks as a faster alternative to USSD for merchant payments. In a standard QR payment flow, the merchant displays a QR code — which may be printed and fixed to the merchant's counter, or generated dynamically by a mobile application for a specific transaction amount — encoding the merchant's payment address or a complete payment request. The payer opens their mobile money or banking application, navigates to the scan function, points the phone's camera at the QR code, reviews the decoded payment details, and authorises the transaction with a PIN.

QR payments require fewer steps than USSD for the payer, eliminating the need to memorise or enter a merchant code manually. However, they introduce a security risk that is absent from USSD: QR substitution fraud. A static QR code printed and displayed at a merchant's point of sale can be covered by a fraudulent QR code sticker placed by a malicious actor, redirecting all payments to an unintended account without any visible indication to the payer. The payer must read the decoded destination carefully before confirming — a step many users skip — and the merchant has no technical means to detect the substitution. Dynamic QR codes generated per transaction mitigate this risk but require the merchant to have a screen capable of displaying the code and an application capable of generating it. Additionally, QR scanning requires the payer to open their payment application, navigate to the scan function, and hold the phone steady while the camera resolves the code — a process that fails in poor lighting or when the printed code is damaged, and that requires the payer to interact actively with their device throughout.

**Bank-Issued Point-of-Sale Terminals**

Bank-issued point-of-sale (POS) terminals represent the most technologically capable existing payment acceptance method in Rwanda. A POS terminal reads payment credentials from a bank-issued card — via magnetic stripe, EMV chip contact, or contactless NFC — and communicates with the acquiring bank over a data connection to authorise the transaction in real time. The merchant receives immediate on-screen confirmation and a printed receipt. The payer's account is debited without delay.

POS terminals provide a reliable, internationally standardised payment experience with real-time confirmation and a clear audit trail. However, their deployment is constrained by access requirements that exclude much of Rwanda's informal and small merchant segment. Acquiring a POS terminal requires the merchant to have a formal bank account, complete a merchant services agreement with an acquiring bank, and typically pay a hardware rental fee or an upfront purchase cost. Terminals require a sustained mobile data or WiFi connection to communicate with the acquiring bank. In environments where connectivity is intermittent, or where a merchant's transaction volume does not justify the recurring cost, these requirements collectively make POS terminals inaccessible. Additionally, bank-issued POS terminals are proprietary hardware controlled by the issuing financial institution, making them unavailable to technology companies or platform operators seeking to operate a closed-loop payment system independent of the traditional banking infrastructure.

## Modeling the Current Payment System

The current payment process for a typical mobile money merchant payment in Rwanda follows a linear sequence of steps distributed across the payer, the mobile money network, and the merchant. The payer initiates the sequence unilaterally, with no real-time awareness on the merchant's side until an SMS confirmation arrives.

The process begins when the merchant communicates their payment code and the amount due to the payer — typically verbally or via a handwritten or printed notice. The payer then dials the USSD shortcode on their phone and begins navigating the menu. The menu session proceeds through merchant code entry, amount entry, and PIN confirmation. The mobile money platform processes the transaction and sends SMS notifications to both the payer and the merchant. The merchant reviews the SMS to confirm receipt, and the transaction is considered complete.

This process is entirely payer-initiated and payer-driven. The merchant has no ability to create a specific transaction record in advance, no visibility into whether the payer has begun the payment process, and no real-time notification channel separate from SMS. If the payer enters the wrong merchant code, the merchant does not receive any funds and has no immediate indication that an error occurred. If the SMS notification is delayed, the merchant cannot confirm receipt at the moment the payer claims to have paid, which creates the potential for disputed transactions.

For QR code payments, the sequence is similar but the payer scans a code rather than entering a merchant code manually. The merchant's role remains passive: they display the code and wait for an SMS confirmation. The absence of a merchant-initiated transaction record means that there is no server-side record of the specific payment request until the transaction completes.

## Analysis of the Existing Systems

The following analysis examines each existing payment method against the specific functional requirements of a fast, secure, and inclusive merchant payment system, identifying the current situation and the improvement that Bizcopay proposes to address it.

**USSD-Based Mobile Money**

*Current Situation:* The payer navigates a four-to-seven-step USSD menu sequence lasting between thirty seconds and two minutes per transaction. Session timeouts and manual entry errors require the session to be restarted. Transaction confirmation arrives by SMS after a variable delay. There is no real-time fraud detection at the moment of transaction initiation; anomaly detection is applied retrospectively. The merchant cannot initiate a structured transaction record in advance, and has no visibility into the payer's progress through the menu.

*Proposed Improvement:* Bizcopay replaces the payer-driven USSD flow with a merchant-initiated transaction. The merchant creates a payment request in the Bizcopay application, which generates a structured transaction record in the backend immediately. The merchant's Android device then reads the payer's NFC token in a single tap, resolving the payer's identity in under a second without any action required from the payer. The fraud engine evaluates the transaction risk immediately. Both parties receive a real-time Socket.IO notification the moment the outcome is determined — no SMS delay, no session timeout, no manual entry.

**QR Code Payments**

*Current Situation:* Static QR codes displayed at merchant points of sale are vulnerable to substitution fraud: a fraudulent QR sticker placed over the legitimate code redirects payments silently to an unintended account. QR scanning requires the payer to open their application, navigate to the scan function, and hold the phone steady against the code — a process that fails in poor lighting or when the code is damaged. Dynamic QR codes reduce the substitution risk but require additional merchant infrastructure. The payment is payer-initiated and the merchant's role remains passive until SMS confirmation arrives.

*Proposed Improvement:* Bizcopay eliminates the QR code entirely. The payer's NFC token — embedded in a ring, card, or band — is a physical chip registered to their account and read by the merchant's device. There is no code to substitute, photograph, or damage. The merchant initiates the tap; the payer does not interact with their own device until the PIN confirmation step. The token cannot be replaced without physical access to the accessory, and the registered UID in the backend is known to the system — any unregistered token resolves to no account and produces an immediate error.

**Bank-Issued POS Terminals**

*Current Situation:* Bank POS terminals provide reliable real-time payment confirmation but require a formal merchant services agreement, hardware rental or purchase, and sustained data connectivity. These requirements exclude informal and small merchants from the formal merchant payment ecosystem. POS terminals are controlled by issuing banks and are not available to platform operators seeking to deploy a closed-loop payment system.

*Proposed Improvement:* Bizcopay uses any NFC-capable Android smartphone as the payment acceptance terminal. The merchant installs the Bizcopay application, registers an account, and is immediately ready to accept payments. No hardware rental agreement, no bank approval, and no dedicated terminal are required. The cost barrier to merchant payment acceptance is reduced to the cost of the Android device the merchant already owns or can acquire at standard consumer retail prices.

## Problems of the Existing System

Analysing the existing payment methods through the PIECES framework — Performance, Information, Economics, Control, Efficiency, and Service — identifies the specific problem dimensions that motivate the Bizcopay design.

**Performance:** USSD payment sequences take between thirty seconds and two minutes to complete, creating queues at high-volume merchant points. QR scanning adds time for application navigation and camera resolution. Neither method delivers simultaneous real-time confirmation to both merchant and payer. Bank POS terminals provide the best performance but are unavailable to most informal merchants. No existing method completes a merchant payment in under ten seconds with both-party confirmation.

**Information:** USSD and QR transactions are confirmed by SMS notifications that may arrive after a variable delay, leaving both merchant and payer in an uncertain state immediately after the transaction. No existing method provides a structured, queryable transaction record that the merchant creates in advance. None of the three methods evaluate transaction risk at the moment the payer is identified; fraud detection, where it exists, is retrospective and manual.

**Economics:** Bank POS terminals carry hardware, service, and connectivity costs that exclude the informal merchant segment. USSD and QR solutions have lower direct costs but impose a time cost on every transaction through slow completion flows. For merchants serving high transaction volumes, the throughput loss from slow payment flows represents an indirect economic burden. Bizcotap NFC accessories represent an investment already made by the consumer for identity and networking purposes; repurposing them for payment adds economic value to an existing asset at no additional cost.

**Control:** None of the three existing methods include automated fraud detection at the moment the payer is identified. USSD and QR systems rely on the mobile money operator's retrospective fraud review processes. POS terminal fraud prevention is delegated to the card scheme and the issuing bank. The merchant, the platform operator, and the system have no mechanism to evaluate transaction risk in real time and decide whether to proceed before the transaction is approved.

**Efficiency:** USSD flows require three to seven selections and entries per transaction. QR scanning requires the payer to open and navigate a payment application, then resolve the code optically. Both processes introduce steps that can fail: USSD sessions time out, QR codes are unreadable in poor conditions, and manual data entry produces errors that require the flow to be restarted. Each failure requires the full sequence to be repeated, multiplying the time cost.

**Service:** USSD and QR payment methods require the payer to initiate and drive a multi-step process during which both the payer and the merchant are occupied and uncertain. The absence of a simultaneous, real-time outcome notification means neither party has a confident confirmation of the transaction's status at the moment it completes. For the payer, the experience is one of active effort under time pressure. For the merchant, the experience is one of passive waiting followed by SMS verification. Neither outcome represents a high-quality payment service.

## Related Work

The NFC-based contactless payment domain has produced both commercial deployments at scale and academic research into security, architecture, and adoption that collectively inform the Bizcopay design.

**Commercial NFC Payment Platforms**

Apple Pay and Google Pay represent the most widely deployed NFC payment systems in commercial use globally. Both systems implement the Host Card Emulation (HCE) protocol, which allows an NFC-capable smartphone to present itself as a contactless payment card to a point-of-sale terminal, communicating with the terminal using the same ISO/IEC 14443 protocol that governs physical contactless cards. The payment credential is tokenised — a device-specific token is generated by the card scheme and stored in the phone's secure element or processed through HCE — so that the actual card number is never transmitted. The payer unlocks their phone, opens or activates the wallet application, and holds the phone against the terminal.

These systems demonstrate the technical feasibility of smartphone NFC payments at scale and the security value of tokenised credentials. However, they are designed for open-loop infrastructure: they require integration with international card schemes (Visa, Mastercard) and are not deployable by local operators or startups seeking to build a closed-loop wallet. They require the payer's device to actively emulate the NFC card — meaning the payer's phone must be unlocked, the wallet application active, and the phone presented to the terminal by the payer. In Bizcopay, the model is inverted: the payer's credential is a passive NFC chip embedded in an accessory worn on the body, and the merchant's device is the active reader. The payer's phone remains in their pocket until the PIN confirmation step.

**Academic Research on NFC Payment Security**

Academic literature on NFC payment security has examined relay attacks, eavesdropping, and cloning as the primary threat vectors against contactless payment systems. A relay attack involves an adversary intercepting and forwarding NFC communication between a legitimate reader and a legitimate token, effectively extending the functional range of the interaction to allow fraudulent transactions. Research has established that NFC's inherent short read range — typically two to five centimetres in practice — provides a meaningful physical constraint that limits passive relay attacks. Active relay attacks using a pair of devices to extend the range have been demonstrated in research settings but require physical proximity to the target accessory and are defeated by distance-bounding protocols and time-of-flight measurement.

Bizcopay's architecture addresses the relay attack risk through server-side validation: each NFC UID is validated against the registered token database before a transaction record is created. An attacker who relays a UID without possessing the corresponding registered accessory produces a transaction against the correct payer account — but that transaction then requires the payer's PIN, which the attacker does not have. The fraud detection engine adds a second layer: an unusual transaction at an unexpected hour or above a threshold amount is flagged and routed to manual PIN confirmation regardless of whether the token read was legitimate.

**Closed-Loop Wallet Systems in Emerging Markets**

Research into closed-loop digital wallet systems in emerging markets has examined the trade-offs between interoperability and platform control. A closed-loop wallet is managed entirely by the platform operator rather than a regulated bank; funds held in the wallet can only be spent within the platform's ecosystem. This design allows the platform to implement custom fraud rules, custom transaction limits, and a tailored user experience without requiring regulatory approval at the level required for open-loop banking infrastructure. The trade-off is that wallet funds cannot be spent outside the platform. Academic work in this area has established that closed-loop wallets are well-suited to controlled environments — employee benefit systems, campus payments, event commerce — where the use case is bounded and the operator can manage the full lifecycle of the payment.

Bizcopay adopts the closed-loop model deliberately. The prototype uses simulated wallet balances rather than real monetary value, allowing the full transaction lifecycle — merchant initiation, NFC resolution, fraud detection, PIN confirmation, balance deduction — to be demonstrated and tested without requiring integration with regulated financial infrastructure. This constraint is explicitly acknowledged as a scope limitation and is identified as a future extension point in Chapter Five.

**NFC in Event Access and Identity Systems**

NFC-based event access control systems share core architectural elements with payment systems: both require a device to read a passive NFC token, resolve the UID to an identity record in a backend system, and make a real-time decision. Research and commercial deployments of NFC event systems — including festival wristband access control and campus ID systems — have established that the read-and-resolve interaction model used in Bizcopay, where the active reading device is operated by the service provider (the merchant or gate operator) rather than the token holder (the payer or attendee), is a secure and practical interaction pattern. The payer's required interaction is limited to presenting the accessory; no application launch or code entry is needed on their side until a confirmation step is required. This pattern has been demonstrated to reduce queue times and error rates compared to manual or code-based alternatives in access control deployments, and Bizcopay applies the same pattern to the merchant payment context.

## Proposed Solution

The analysis of existing payment methods and the review of related work establish a clear basis for the Bizcopay design. The proposed solution is an NFC-based contactless payment system that addresses each identified gap through a set of specific architectural decisions.

Passive NFC tokens embedded in Bizcotap accessories — rings, cards, stretch bands, and phone cases already in consumers' possession — serve as payment credentials. Each token is registered to a payer's account in the Bizcopay backend by recording the token's unique hardware identifier (UID). No proprietary hardware is required beyond the accessory itself. The payer does not need to take any action to present their credential beyond being within NFC read range of the merchant's device.

The merchant operates any NFC-capable Android smartphone running the Bizcopay application. The merchant creates a payment request — specifying the amount — before the NFC tap, generating a structured transaction record in the backend immediately. The merchant's device then reads the payer's NFC token, resolving the payer's identity in under a second and linking it to the pre-created transaction record. This replaces both the payer-driven USSD flow and the passive QR display with a merchant-initiated, NFC-resolved transaction model.

A real-time fraud detection engine evaluates every transaction at the moment of NFC resolution, before the payer is prompted to enter a PIN. The engine applies configurable rules — detecting abnormally large amounts, rapid successive transactions from the same payer, and activity at unusual hours — and routes each transaction to automatic approval, PIN confirmation with an audit record, or immediate rejection. This moves fraud evaluation from after-the-fact SMS review to real-time gate-keeping at the point of identity resolution.

Real-time Socket.IO notifications deliver the transaction outcome to both the merchant's and the payer's devices simultaneously, replacing delayed SMS confirmation with an immediate, channel-specific event. The merchant's application reflects the transaction status change without any manual refresh, and the payer receives a notification on their device the moment the outcome is determined.

The complete system — a Node.js and TypeScript backend API, a Kotlin Jetpack Compose Android application, and a Next.js administration panel — is designed and implemented as an integrated platform that demonstrates how existing NFC hardware can anchor a secure, frictionless, and locally deployable contactless payment system tailored to the Rwandan merchant context.
