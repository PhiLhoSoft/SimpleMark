# SimpleMark: a Simple Humane Markup

The following specifies a text markup inspired by Markdown, but simplified to ease parsing, learning and usage.
It is closer to the simplified version used in [Google+](http://webapps.stackexchange.com/questions/23078/what-are-all-the-formatting-options-for-a-google-post) (simplified bold / italic marks), [GitHub](https://help.github.com/articles/markdown-basics/) ([aka. GitHub Flavored Markdown](https://help.github.com/articles/github-flavored-markdown/), newlines mark line breaks, blocks of code) or [Stackoverflow](http://stackoverflow.com/editing-help) than to the original spec.
It aims more at writing short comments than writing big, complex documents. For the latter, something like [CommonMark](http://commonmark.org/) is better suited.

One of the main departure from the original spec is that the newline character does a line break. No mandatory empty line nonsense, that confuse so much users and often result in mangled messages. Text area in browsers and in modern editors automatically wrap long lines, so auto-join of consecutive lines is an outdated feature.

As a consequence, it simplifies greatly the spec (and the parsing!) and the usage of the markup.

Likewise, like Google+, I favor one character signs for styling fragments, with distinct usages. The fact that * and _ have the same rendering in Markdown, and ~** and ~__ a different rendering (and ~*** or ~___ to combine them!) looks quite confusing...

The markup uses Ascii characters in a given context to apply a special style (eg. HTML markup + CSS) to whole lines of text (blocks, div-like in HTML) or to fragments of text (span-like in HTML).
These special characters can loose their meaning in some context, and can always be escaped with the tilde `~~` sign preceding them.
If tilde precedes a non-markup character, it is kept literal. It can also be doubled to figure a literal tilde.

Block markup characters are defined at the start of a line, regardless of initial spaces.
Fragment markup characters loose their meaning if surrounded by spaces or by letters or digits.
A fragment of text stops at the end of the line: authors rarely want to have several paragraphs of bold text, and thus a missing ending char doesn't spread over the whole remainder of the text.

### Limitations

There is no support for blockquotes (I prefer to use double quotes surrounding an italicized citation), tables or images. No HTML markup can be used, `&` ,`<` and `>` signs are escaped (kept literal) in an HTML rendering.


## Styles

Fragments of text can be bold, italic, stroked through or with fixed font.
In HTML, they are rendered respectively with the `strong`, `em` (emphasis), `del` (deleted) and `code` tags.

The markup uses respectively star `*`, underscore `_`, dash `-` and backtick `~`` surrounding the fragment.
There can be no space after the initial sign, and no space before the ending sign.
There can be no letter or digit before the initial sign, or after the ending one.

So ~*bold~* and ~*this is bold~* are valid markup, seen as *bold* and *this is bold*. But x*y and x * y remain literal.
"~_This is a citation~_" is also valid markup (shown as "_This is a citation_"), but CONST_NAME is kept as is.
~-striked through~- is shown as -striked through-, but in-line or a - b are kept as is.
Code fragments can be shown with a fixed font (`code` tag in HTML) by surrounding them with backticks: ~`int x = 0;~` will show as `int x = 0;`.
Inside a code fragment, markup characters (except tilde) loose their meaning.

Fragment styling can be nested:
This sentence has ~_italic parts ~*and bold~* too~_.
becomes:
This sentence has _italic parts *and bold* too_.

The ending signs must be in reverse order of the starting one:
This is ~*~_strong emphasized~*~_ text.
will be displayed as:
This is *_strong emphasized*_ text.
because you cannot have a new bold style inside a fragment that is already bold, and the second star doesn't end the first one because a new style has been started and not ended yet.


## Links

A link can be made explicitly by wrapping the link text in brackets `[]`, followed by the link itself in parentheses `()`.
Example: ~[A well known destination](http://www.google.com) or ~[Popular programming site](https://github.com) or ~[Relative link](../foo/bar.html) become:
[A well known destination](www.google.com) and [Popular programming site](https://github.com)  or [Relative link](../foo/bar.html).
The link text can have markup signs in it.

### URL autolinking

URLs starting with a common schema (http://, https://, ftp://, ftps://) are automatically turned into a link to that URL. The URL conversion stops on some characters, that should be escaped if they are part of the URL. Or use the explicit form. Unlike some autolinking libraries, SimpleMark doesn't attempt to guess an URL if it has no schema (eg. autolinking google.com or www.example.com/whatever).

Markup signs are ignored while parsing an URL.
The link text will be the URL without the schema (to be shorter). If the URL is longer than a predefined (ajustable) length, it will be shortened with ellipsis.


## Titles

SimpleMark has only three levels of title.
In HTML rendering, they are not necessarily mapped to `h1` to `h3`. They might be mapped to `h3` to `h5`, for example, or even be just `div`s with their own classes.
These levels are denoted as a series of one to three sharp signs `#` at the start of the line, followed by a space. One `#` denotes the highest level, three is for the lowest one.
Titles should be rendered with a bolder font, with size bigger than main text, and some vertical space before and after the line.

Example:
~## Second level title


## Paragraph

A line break is rendered by a simple line break, ie. in HTML a `br` tag.
An empty line (or several consecutive ones) separates paragraphs, rendered in HTML with a `p` tag.


## Lists

Unordered lists are made with at least two consecutive lines starting with a dash `-` or a plus `+` or a star `*`, followed by a space.
Ordered lists  are made with at least two consecutive lines starting with a number (sequence of digits) followed by a dot and a space. The numbers are actually ignored, numbering is done automatically from 1.
No nesting is handled. A list stops with an empty line, so there can be two distinct consecutive lists.

~* Item
~* Other item
~* Last item
becomes:
* Item
* Other item
* Last item

~- Item
~- Other item
~- Last item
becomes:
- Item
- Other item
- Last item

~+ Item
~+ Other item
~+ Last item
becomes:
+ Item
+ Other item
+ Last item

~1. Item 1
~1. Item 2
~10. Item n
becomes:
1. Item 1
2. Item 2
3. Item n


## Code blocks

Like in GitHub, a series of three backticks `~`~`~`` on their own line renders all the following lines as code (in a `pre` block in HTML, with `code` style), until another line with `~`~`~`` is met.


# Parsing rules

The fragment (in-line) parser is autonomous, this allows to have an even simpler parser, eg. for writing short comments a la Stackoverflow.

When an URL is parsed (with another autonomous parser), special chars are ignored, until the end of the URL is detected. Details of the URL parsing is to follow.

Outside URLs, tilde characters allow to remove a special meaning to markup signs, anywhere they are found. If not followed by such markup sign, tildes are literal.

Markup signs are:
- For fragments: ~ * _ - ` [ ] ( )
- For blocks (at start of line): # * - + digit (followed by dot) ` (followed by two others)