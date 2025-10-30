# jmpr

A multimedia player application based on Java, supporting playback of local files and remote API media streams.

## Features
- Supports playback of local audio files
- Supports playback of remote media files via API endpoints
- Provides playback controls (play/pause/stop/previous/next)
- Supports playlists and playback history
- Supports ID3 metadata display
- Supports album cover display
- Supports playback speed adjustment
- Supports volume adjustment
- Supports scrubbing through playback progress

## Technology Stack
- Java 8+
- Swing GUI
- FFmpeg (optional)
- HTTP client for API communication
- Embedded database for managing API configurations and playback history

## Installation Instructions
1. Ensure Java 8 or higher is installed
2. Download the `mper.jar` file
3. Double-click to run the JAR file, or run via command line:
   ```
   java -jar mper.jar
   ```

## Usage Instructions
1. After launching the application, use the "File" button to select local media files
2. Add remote media servers via the API management feature
3. Select media files from the playlist to play
4. Use the control panel to manage playback, adjust volume, and change playback speed
5. Supports displaying ID3 metadata and album covers for media files

## Build Instructions
The project is built using Maven:
1. Clone the repository
2. Run `mvn clean package` to build the project
3. The build output will be generated in the `target/` directory

## Project Structure
- `src/main/java/yxs/a02/mper` - Main application and core functionality
- `src/main/java/yxs/a02/mper/model` - Data model classes
- `src/main/java/yxs/a02/mper/service` - Business logic and services
- `src/main/java/yxs/a02/mper/ui` - User interface components
- `src/main/resources` - Resource files

## License
This project is licensed under the Apache 2.0 License. See the LICENSE file in the project for details.