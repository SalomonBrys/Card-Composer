# Demo Deck

This is a demo card deck generated using **Card Composer**. It serves as an example of what can be achieved with the tool and provides a complete, playable set of cards.

## Hand Contents

This deck contains a total of 58 cards:
- **52 Poker cards**: The standard set of four suits (Hearts, Diamonds, Clubs, Spades) from Ace to King.
- **2 Jokers**.
- **4 Additional Tichu cards**: The Mahjong, the Dog, the Phoenix, and the Dragon.

### About Tichu
If you are into card games and do not know **Tichu**, you're in for a treat! It is an incredible partnership climbing game that offers deep strategy and endless fun. You can learn more about it on its [Board Game Geek page](https://boardgamegeek.com/boardgame/215/tichu). Seriously, give it a try!

## Technical Details

This deck was crafted using:
- **[Card Composer](https://github.com/SalomonBrys/Card-Composer)**: The engine used to generate and layout the cards.
- **Default Compose Font**: Used for all numbers and suit symbols.
- **Material Icons**: Used for card icons.
- **Mandala Images**: Used as beautiful illustrations for the card centers.

## How to get a printable version

To generate your own printable PDF or PNG files of this deck, follow these steps:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/SalomonBrys/Card-Composer.git
   cd Card-Composer
   ```

2. **Run the demo app**:
   Navigate to the `Demo-Deck` folder and run the application:
   ```bash
   cd Demo-Deck
   ./gradlew run
   ```

3. **Export the cards**:
   - Once the app is running, click on the **export button** (located in the top right corner).
   - Configure your export settings (paper format, cards, etc.) to your liking.
   - Enjoy your new printable deck!
