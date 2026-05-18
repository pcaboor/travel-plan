FROM node:22-alpine AS build
WORKDIR /workspace
COPY package.json package-lock.json* ./
RUN if [ -f package-lock.json ]; then npm ci; else npm install --no-audit --no-fund; fi
COPY tsconfig.json vite.config.ts postcss.config.js tailwind.config.ts index.html ./
COPY src ./src
RUN npm run build

FROM nginx:1.27-alpine
RUN apk add --no-cache openssl \
    && mkdir -p /etc/nginx/certs \
    && openssl req -x509 -nodes -newkey rsa:2048 -days 825 \
        -subj "/C=FR/O=TravelPlan/OU=Dev/CN=travelplan.local" \
        -addext "subjectAltName=DNS:localhost,DNS:travelplan.local,IP:127.0.0.1" \
        -keyout /etc/nginx/certs/server.key \
        -out  /etc/nginx/certs/server.crt \
    && chmod 600 /etc/nginx/certs/server.key
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist /usr/share/nginx/html
EXPOSE 80 443
HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
    CMD wget -q --no-check-certificate -O /dev/null https://localhost/ || exit 1
