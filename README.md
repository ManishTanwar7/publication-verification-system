# Research Publication Verification & Management Web System

A modern cloud-ready web application built with **Java (Spring Boot 3)**, **Crossref REST API**, and **Tailwind CSS**, designed for verifying scholarly research publications (Title, DOI, Authors, Year, Journal) and managing academic records.

---

## 🌟 Key Features
- **🌐 Full Web Interface**: Responsive dashboard accessible on desktop, tablet, and mobile.
- **⚡ Fast DOI Auto-Import**: Enter any DOI (e.g. `10.1109/CVPR.2016.90`) to automatically fetch official title, authors, year, and container from the Crossref registry.
- **🔍 Automated Online Verification**: Compares submitted data against the official publisher registry with fuzzy title matching and discrepancy alerts.
- **📊 Real-Time Metric Cards**: Instant summary of Total, Verified, Pending, and Discrepancy counts.
- **🔎 Real-Time Search & Status Filtering**: Instant search across Title, Authors, DOI, and Journal.
- **☁️ Cloud Ready for Render**: Pre-configured with a multi-stage `Dockerfile` and `render.yaml`.

---

## 🚀 Deploying to Render.com (Free & Fast)

1. **Push to GitHub**:
   Make sure this project is pushed to your GitHub repository:
   `https://github.com/ManishTanwar7/publication-verification-system`

2. **Open Render**:
   - Go to [dashboard.render.com](https://dashboard.render.com) and log in with your GitHub account.

3. **Create New Web Service**:
   - Click **New +** → **Web Service**.
   - Select **Build and deploy from a Git repository**.
   - Connect and choose **`publication-verification-system`**.

4. **Configure Settings**:
   - **Language / Runtime**: Choose **Docker** (Render will automatically detect the `Dockerfile`).
   - **Instance Type**: Select **Free**.
   - Click **Deploy Web Service**!

Render will build the Docker container and provide a live URL like:
`https://publication-verification-system.onrender.com`!

---

## 💻 Local Testing (Optional)

If running locally with Maven:
```bash
mvn spring-boot:run
```
Then open: `http://localhost:8080` in your web browser.
