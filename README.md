# Quiz Leaderboard System

## Overview
This project fetches quiz data from an API, removes duplicate entries using (roundId + participant), and generates a leaderboard.

## Features
- Poll API 10 times
- Retry logic for server failures
- Deduplication using HashSet
- Score aggregation
- Sorted leaderboard
- API submission

## Output
Bob : 295  
Alice : 280  
Charlie : 260  
Total Score = 835

## Tech Stack
- Java
- HTTP Client (Java 11+)
- org.json library

## How to Run
javac -cp ".:lib/json-20251224.jar" LeaderboardApp.java  
java -cp ".:lib/json-20251224.jar" LeaderboardApp
