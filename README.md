# Android Shopping App

College project at ORT Braude. A shopping app for Android with customer and admin roles, built with Java and Firebase Realtime Database.

## Features
- Sign up and log in; the session is stored locally with SharedPreferences
- **Customers:** browse products, add to cart, change quantities, remove items, and check out
- **Admins:** add, update and delete products and stock
- Checkout validates stock, deducts inventory, and removes sold-out items from carts
- Single host activity with fragment-based bottom navigation (tabs differ by role)
- Product and cart lists stay live-synced with Firebase

## Tech
Java, Android (Fragments, ListView/ArrayAdapter, SharedPreferences), Firebase Realtime Database, Gradle (Kotlin DSL)

