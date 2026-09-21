\# Text-to-Speech Application



A web app that converts written text into natural-sounding speech using the ElevenLabs API.



\## Tech Stack



\*\*Backend:\*\* Java 17, Spring Boot 3.4.1

\*\*Frontend:\*\* React 18, Vite, Tailwind CSS

\*\*TTS Provider:\*\* ElevenLabs



\## Features



\- Text input with character / word count

\- Multi-language selection

\- 21+ voices

\- Generate, play, download MP3 audio

\- Validation and error handling

\- Rate limiting



\## Setup



\### 1. Get ElevenLabs API Key



1\. Sign up at https://elevenlabs.io

2\. Go to Profile → API Keys → Create

3\. Copy the key (starts with sk\_)



\### 2. Configure Backend



Edit `src/main/resources/application.properties`:



tts.provider=elevenlabs

tts.elevenlabs.api-key=sk\_your\_key\_here

tts.elevenlabs.base-url=https://api.elevenlabs.io/v1

tts.elevenlabs.model-id=eleven\_multilingual\_v2



\### 3. Run Backend



cd backend

mvn spring-boot:run



Backend runs on http://localhost:8080



\### 4. Run Frontend



cd frontend

npm install

npm run dev



Frontend runs on http://localhost:5173



\## API Endpoints



| Method | Endpoint | Description |

|--------|----------|-------------|

| GET | /api/health | Health check |

| GET | /api/voices | List all voices |

| POST | /api/tts | Generate speech |



\## Status Codes



\- 200: Success

\- 400: Invalid request

\- 429: Rate limit exceeded

\- 500: Internal server error

\- 503: TTS provider unavailable

