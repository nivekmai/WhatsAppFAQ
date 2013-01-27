WhatsAppFAQ
===========

This is simply an activity that sits between the "Contact us" screen and the email generation activity.

It passes the email body through faq/search.php, dissecting the json result and displaying the html strings (all I can get from search.php without modifying it) in a scrollable list of webviews.

Currently the list implementation is sub-par, it is just 10 buttons and  webviews in a scrollview, ideally I would update it to be a custom list, but for only 10 views it wasn't worth the time.

I could also probably use some help on properly organizing the code, it's not the greatest right now, but I didn't have enough time to learn how to properly organize the code.
