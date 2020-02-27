# Word search-volume

SearchVolume is a micro service which calculates how often Amazon customers search 
for an input keyword. The algorithm is based on an 
[Amazon suggestion API](https://completion.amazon.com/api/2017/suggestions?mid=ATVPDKIKX0DER&alias=aps&prefix=iphone) 
 and return score in the range [0 → 100].

### How to use it?
clone the repository: 
```
git clone https://github.com/AlexShnaider/WordSearchVolume.git
```
go to project directory: 
```
cd WordSearchVolume/
```
start the application:
```
mvn spring-boot:run
```
sent request with a word we want to calculate search-volume, for example "iphone":
```
http://localhost:8080/estimate?word=iphone
```
get word search-volume:
```
{
    "word" : "iphone",
    "searchVolume" : 94
}
```
### What assumptions are made?
The Amazon suggestion API returns up to 11 suggestions when you start writing an input word.
![Alt text](https://raw.githubusercontent.com/AlexShnaider/WordSearchVolume/master/src/main/resources/pictures/amazon_search.png?raw=true)

* If Amazon API returned 11 suggestions first one is for the widget 
(suggesting in which category of the Amazon customer can find this product);

* Widget suggestion is is more important and carry more weight than others in set;

* If Amazon API returned 10 or less suggestions, there is no suggestion for the widget;

* The search-volume calculated based only on the first word of suggestion. 
For example in suggestion *"juicer for celery juice"*  the *"for celery juice"* is discarded;

* Suggestion set from shorter prefix is more important and carry more weight 
then set from a longer prefix;

### How does the algorithm work?

The search-volume is a sum of weight factors if the first word of a suggestion is equal to the initial word.
The sum consists of four suggestions set. 

The suggestions are made for the prefix of one, two, three starting 
letters of the initial word and for the whole word. If the initial word is shorter than four letters, 
the whole word is used in set correspondingly.

The shorter prefix carries more weight and the widget suggestion is double the weight.
Next table is represents weight factors:

| prefix | widget factor | usual factor |
| :---: | :---: | :---: |
| 1 | 8 | 4 |
| 2 | 6 | 3 |
| 3 | 4 | 2 |
| whole word | 2 | 1|

To have a result in a 0..100 range, after summing all four sets it is multiplied by 
normalization factor of 0.83.

### Is the order of the Amazon returned suggestions insignificant?

Well, if we took in consideration the widget suggestion it is significant, because widget suggestion,
if present, returned first. Also we can differentiate suggestions in one set based on a order as well, because 
we can assume that the higher suggestion is more important.

Although, the distinction in weight of different sets should be much more significant in my opinion.
Thus, other than widget suggestion we can drop out the differences in one set.

### Is the outcome precise enough?

It is hard to tell, apart from calculating search-volume for the hype words such as "iphone", "apple" 
and verify that it is quite high and check that not existing words, such as "sdgssg", 
or rare used words such as "pfirsich" have zero or low search-volume.

Another problem is that really popular words are shadowing less popular words starting with 
the same letters and they can have a disproportionately lower search-volume. 
For example “iphone” (search-volume 94)) and “ipad” (search-volume 36). 
“Iphone” is so popular that it took all the suggestions on the first two prefixes 
and made “ipad” disproportionately less popular than, i think, it is in reality.

There is also place to play with the weight ratios, weather or not to use widget suggestion
or take more sets with longer prefixes.


For example, there is also available linear REST API: 
```
http://localhost:8080/estimateLinear?word=iphone
```
which return:
```
{
    "word" : "iphone",
    "searchVolume" : 96
}
```

It is simplified algorithm which doesn't take into account widget suggestion 
and the difference of sets. All suggestions just summed up and normalized by multiplying on 2.27 
Next table is represents weight factors:

| prefix | widget factor | usual factor |
| :---: | :---: | :---: |
| 1   | 1 | 1 |
| 2   | 1 | 1 |
| 3   | 1 | 1 |
| whole word | 1 | 1|
