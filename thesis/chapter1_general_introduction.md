# CHAPTER 1

# GENERAL INTRODUCTION

## Introduction

The rapid evolution of digital technologies has transformed the way financial transactions are conducted across the world. Among the most significant innovations reshaping the payments landscape is Near Field Communication (NFC) technology, a short-range wireless standard that enables two devices to exchange data when brought within a few centimetres of each other. Originally used in access control cards and public transport ticketing, NFC has increasingly been adopted for contactless retail payment solutions, enabling consumers to pay by simply tapping a smartphone, ring, wristband, or smart card against a compatible terminal.

In Africa, the need for fast, secure, and inclusive digital payment systems has grown considerably. Mobile money services have demonstrated the continent's appetite for cashless alternatives, yet they remain largely dependent on USSD menus and QR codes that are slow, error-prone, and friction-heavy for merchants. Countries like Rwanda, which have committed to ambitious digital transformation goals, require next-generation payment infrastructure that is frictionless, fraud-resistant, and capable of serving both merchants and consumers in real time.

This project, **Bizcopay**, is an NFC-based contactless payment system built with a closed-loop digital wallet and real-time fraud detection. It enables a merchant to initiate a payment request, read a payer's NFC token using an Android device, and complete a transaction in seconds — all without requiring the payer to open an application or enter credentials until the final PIN confirmation step. The system draws direct inspiration from the products and business model of **Bizcotap**, a technology company that manufactures NFC and QR-code-enabled accessories including smart rings, business cards, stretch bands, and phone cases. Bizcotap's hardware forms the physical NFC token layer that this payment system is designed around.

Technically, the system is built using modern, production-ready technologies. The backend is developed using Node.js with Express and TypeScript, providing a RESTful API with real-time Socket.IO communication and Prisma ORM for database management. The Android application is built with Kotlin and Jetpack Compose, offering a clean and responsive interface for both merchants and payers. The administration panel is developed using Next.js, giving administrators visibility over users, transactions, and fraud alerts. Together, these components form a complete, integrated payment platform that demonstrates how existing NFC hardware can anchor a secure and functional contactless payment solution.

Beyond solving a technical challenge, Bizcopay contributes to a broader conversation about digital financial inclusion in Rwanda. By replacing expensive point-of-sale terminals with an Android smartphone and commodity NFC accessories, the system lowers the barrier to merchant payment acceptance and demonstrates that locally developed systems can meet high professional and security standards when built with care and discipline.

## Background of the Study

The global shift toward contactless payments has accelerated in recent years, driven by consumer demand for faster and safer transaction experiences. NFC technology, standardised under ISO/IEC 14443 and ISO/IEC 18092, operates at 13.56 MHz with a read range of a few centimetres, providing inherent physical security. When combined with server-side wallet management and automated fraud detection, NFC provides a robust foundation for consumer payment systems that is already proven in markets across Europe, North America, and Asia.

Bizcotap is a technology company specialising in NFC and QR-code-enabled accessories designed to make physical-to-digital interactions instantaneous. Their product catalogue includes smart NFC rings worn on the finger, smart business cards in PVC or metal embedding an NFC chip alongside a printed QR code, silicone stretch bands carrying NFC chips popular for events and access control, and phone cases with adhesive or integrated NFC tags. Bizcotap products are compatible with Apple devices from iPhone 7 onwards, Samsung Galaxy devices from 2014 onwards, and all Android devices running Android 10 and above with NFC hardware. This broad compatibility makes Bizcotap accessories suitable as payment credential tokens without requiring users to own any proprietary hardware beyond the accessory itself.

Bizcotap's current business model focuses on identity and networking applications — sharing contact information, linking to social media profiles, and enabling event check-in. However, the same NFC hardware is technically capable of serving as a payment token when backed by a secure server infrastructure. This project extends the Bizcotap product vision into the payments domain, demonstrating how existing NFC accessories already in consumers' hands can anchor a full contactless payment platform without additional hardware investment.

Rwanda's digital payment infrastructure has made considerable progress in recent years, with mobile money operators providing person-to-person transfer services and merchant payment options. Nevertheless, several gaps remain for small and medium merchant scenarios. USSD-based mobile money merchant payments require the payer to navigate multi-step menus and enter credentials under session timeouts, a process that is slow and error-prone. QR code payments carry the risk of substitution fraud, where a fraudulent QR code replaces a legitimate merchant's code. Additionally, existing bank-issued point-of-sale terminals carry significant upfront cost that places them out of reach for many informal merchants. Bizcopay is designed to address each of these gaps directly.

## Statement of the Problem

Despite the proliferation of mobile money services in Rwanda and the growing availability of NFC-capable hardware at accessible price points, no locally deployed payment solution combines NFC token reading, a closed-loop digital wallet, real-time automated fraud detection, and a merchant-facing Android application into a single integrated platform. Existing merchant payment workflows remain slow, prone to fraud, and dependent on either cumbersome USSD flows or expensive POS hardware.

Furthermore, the growing availability of NFC wearables such as the rings, wristbands, and cards produced by companies like Bizcotap creates an untapped opportunity. Consumers and merchants already have access to NFC-enabled accessories, yet no platform allows these accessories to function securely as payment credentials linked to a digital wallet.

The specific problems this project addresses are the following:

- **Slow and friction-heavy merchant payment flows:** Current USSD and QR-based payment methods require multiple steps, manual data entry, and session management that slow down transactions and create a poor experience for both merchants and payers.

- **Absence of a platform for NFC wearables as payment tokens:** No existing system in Rwanda allows commodity NFC accessories such as rings, wristbands, and cards to function as registered payment credentials linked to a digital wallet.

- **Lack of real-time fraud detection at the point of NFC resolution:** Existing systems do not evaluate transaction risk automatically at the moment a payer is identified, before the transaction is approved or rejected.

- **High cost of merchant payment acceptance hardware:** Bank-issued POS terminals require significant upfront investment, excluding small and informal merchants from accepting digital payments efficiently.

## Choice and Motivation of the Study

The choice of this study is driven by the need to demonstrate that an NFC-based contactless payment system can be built using accessible technologies, grounded in a real case study, and capable of addressing genuine gaps in Rwanda's merchant payment landscape. After studying Bizcotap's NFC hardware and analysing the limitations of existing USSD and QR-based payment methods, the researcher was motivated to design a system that brings together NFC token reading, wallet management, fraud detection, and real-time notifications into a single coherent platform.

**To AUCA:** This project aligns directly with the mission of the Adventist University of Central Africa, which promotes academic excellence, innovation, and the application of Information Technology to real-world problems. By undertaking the design and development of Bizcopay, the researcher applies knowledge gained in software engineering, database design, mobile development, and system security. This work demonstrates that AUCA students are capable of building complete, production-quality systems that respond to the technological needs of African communities.

**To Bizcotap:** For Bizcotap, this project represents a proof of concept that extends the application of their existing NFC hardware beyond networking and identity into the payments domain. The system shows that Bizcotap rings, cards, stretch bands, and phone cases can function as secure payment credential tokens when backed by a robust server infrastructure, opening a potential new market for their products and demonstrating the versatility of their technology.

**To the Student:** For the researcher, this project is both a personal and academic milestone. It transforms theoretical knowledge into a practical system that addresses a real problem. The experience of designing an API, building an Android application, implementing a fraud detection engine, and integrating all components strengthens technical expertise and analytical thinking. It also reflects a genuine belief that locally developed systems can meet high professional and security standards when built with discipline and care.

## Objectives of the Study

**General Objective**

The primary aim of this project is to design, implement, and evaluate Bizcopay, an NFC-based contactless payment system that uses NFC wearable tokens inspired by the Bizcotap product line as payment credentials linked to a closed-loop digital wallet with real-time fraud detection.

**Specific Objectives**

- To analyse the existing contactless payment landscape in Rwanda and identify the functional and security gaps that an NFC-based closed-loop system can address.
- To design a system architecture comprising a Node.js and TypeScript REST API backend, a Kotlin Android merchant and payer application, and a Next.js web-based administration panel communicating in real time via Socket.IO.
- To implement a secure authentication and wallet management module with bcrypt PIN hashing, JWT-based session management, and email OTP verification for registration and PIN reset.
- To implement a fraud detection engine that evaluates transaction risk in real time at the moment of NFC token resolution and routes transactions to automatic approval, PIN confirmation, or rejection based on configurable rules.
- To implement a merchant-facing Android interface that uses the device's built-in NFC reader to resolve payer tokens and initiate the payment flow without requiring the payer to interact with their own phone until PIN confirmation.
- To evaluate the system through functional unit testing of all core services and a simulated end-to-end payment flow covering the full transaction lifecycle.

## Scope of the Study

This study focuses on the design and implementation of a complete NFC-based contactless payment system demonstrated through a working prototype. The system covers user authentication including registration with OTP email verification, login, and PIN reset; wallet management including balance queries; transaction management covering creation, NFC resolution, fraud evaluation, and PIN-based approval; NFC token registration and management; and real-time Socket.IO event emission to notify both merchants and payers of transaction outcomes. An Android application covers the authentication flow, a merchant dashboard for creating transactions and reading NFC tokens, and a payer dashboard for viewing balances and confirming pending payments. A Next.js administration panel provides visibility into users, live transactions, and fraud alert logs.

The study does not extend to integration with Rwanda's national payment interoperability infrastructure, the use of real monetary value in wallets as the system uses simulated balances, the development of an iOS application, formal compliance certification, or production cloud deployment. Physical NFC token testing is performed where a compatible Android device is available; otherwise the NFC tap step is simulated through the Android application. These boundaries ensure that the study remains focused on demonstrating the core technical innovation within an academic timeframe while providing a solid foundation for future extension.

## Methodology and Techniques Used in the Study

The design and development of Bizcopay followed a structured methodological approach. Understanding the technical landscape, the limitations of existing systems, and the capabilities of the tools and technologies involved was essential to ensuring the system was built on a sound foundation. To support this process, the study used one primary data collection technique: documentation.

**Documentation**

Documentation refers to the systematic review and analysis of existing materials such as technical specifications, official product documentation, framework guides, and academic and industry literature. It helps in understanding how existing systems work, what standards govern them, and what gaps or limitations exist in current approaches. This method provides a factual and verifiable basis for design decisions and ensures the system is built in alignment with established best practices.

In this study, the researcher reviewed the official documentation for all technologies used in the project, including the Node.js and Express framework documentation, the Prisma ORM documentation, the Android Jetpack Compose and NFC API guides, the Next.js framework documentation, and the Socket.IO specification. The NFC standards ISO/IEC 14443 and ISO/IEC 18092 were studied to understand how NFC communication works at the hardware and protocol level. The Bizcotap product documentation and compatibility specifications were reviewed to understand which devices are supported and how the NFC chips in their accessories are structured. Additionally, existing literature on mobile payment systems, fraud detection approaches, and digital wallet architectures was consulted to inform the system design and fraud rule thresholds. This review of documentation directly shaped the architecture of the system, the choice of authentication mechanisms, and the design of the fraud detection engine.

## Expected Results

The implementation of Bizcopay is expected to produce a working demonstration of a complete NFC-based contactless payment flow, from merchant transaction creation through NFC token resolution and fraud evaluation to payer PIN confirmation and balance deduction. Upon successful completion of the project, the following outcomes are expected:

**A fully functional backend API:** The Node.js and TypeScript server implementing all payment system endpoints, with automated unit tests covering authentication, wallet management, fraud detection, and transaction services demonstrating correct and reliable behaviour across all core modules.

**A working Android application:** The Kotlin Jetpack Compose application demonstrating the end-to-end payment flow, where a merchant creates a transaction, reads a payer's NFC token, the fraud engine evaluates the risk level, the payer confirms with their PIN, and both parties receive real-time Socket.IO notifications of the outcome.

**A Next.js administration panel:** Providing administrators with live visibility into registered users, transaction history and status, and fraud alert logs with real-time updates through Socket.IO.

**Demonstrated NFC payment completion:** A complete transaction from merchant initiation to payer balance deduction demonstrated through functional testing, validating that the system performs all steps correctly and in the expected sequence.

**Email OTP security:** Verified email-based one-time password delivery for both registration and PIN reset, with the OTP stored as a bcrypt hash in the database and invalidated after use, demonstrating a secure two-step verification flow.

**A documented system:** Complete source code in a version-controlled repository with a clear architecture description, database schema, and API structure suitable for peer review and future extension.

## Organization of the Work

This thesis is organized into five chapters, each addressing a key stage in the analysis, design, development, and evaluation of the Bizcopay NFC payment system.

**Chapter One** presents the general introduction, including the background of the study, the statement of the problem, the choice and motivation, the objectives, the scope, the methodology, the expected results, and the organization of the work.

**Chapter Two** analyses the existing payment systems currently used in Rwanda and similar markets, examining USSD-based mobile money, QR code payments, and bank-issued POS terminals. It identifies the functional and security gaps that motivate the Bizcopay design and reviews related work in the NFC payment domain, including an overview of Bizcotap's products and technology.

**Chapter Three** presents the requirements analysis and system design of Bizcopay, including functional and non-functional requirements, the full system architecture, database schema, API contract, UML diagrams, and security design decisions.

**Chapter Four** describes the implementation of each system component — the backend API, the Android application, and the administration panel — with key code excerpts illustrating the design of the authentication module, fraud detection engine, real-time Socket.IO communication, and NFC token resolution flow. It also presents the testing approach and results.

**Chapter Five** summarises the research findings, evaluates the system against the stated objectives, reflects on the limitations encountered during the project, and proposes directions for future work including integration with Rwanda's national payment infrastructure and extension to additional platforms.
