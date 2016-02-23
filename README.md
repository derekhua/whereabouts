## Inspiration
We all agreed that keeping up/meeting with friends is sometimes hard. That's we we created ~**Whereabouts!**~

## What it does
Whereabouts lets users create chatrooms, where they can invite all their friends. But wait, there's more! – It also lets users view the locations of all their friends in the chatroom in realtime! This allows users to better connect, meet-up, and in general bring people together.

## How we built it
Whereabouts is an Android mobile application. We built the client with canonical Android, with the addition of Google Maps API, Facebook login, GPS, and the Socket.io framework for keeping connected with the server. On the backend, we have a Node.js server running on an Amazon EC2 instance. This server is utilizing Socket.io for web-sockets, and Express.js.

## Challenges we ran into
* GPS precision
* Server – Client communication
* Storing data

## Accomplishments that we're proud of
We're proud that we implemented a functional chat-room application, and group position tracker.

## What we learned
* Making Android apps is hard
* using the Google Maps API
* Android permissions and storage
* Web-sockets
