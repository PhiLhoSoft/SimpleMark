package org.philhosoft.parser;

// Inspired by Scintilla's StyleContext class.
// Usually, I start my method names with verbs or do real getters, but here I took at more concise approach, for readability.
// Closer of Ceylon style with direct access to properties.
/**
 * Allows to "walk" through a string, character by character, always forward but keeping an eye on the immediate past (and future!).
 * <p>
 * Intended to be used by a parser, abstracting the concept of end-of-line (EOL): the current character is always only newline on a line break,
 * Windows line breaks are seen as only one char.
 */
public class StringWalker
{
	private static final char PLACEHOLDER_CHAR = '\0';

	private String walked;
	private int cursor;
	private boolean atLineStart, atLineEnd;

	private char previous = PLACEHOLDER_CHAR;
	private char current = PLACEHOLDER_CHAR;
	private char next = PLACEHOLDER_CHAR;

	public StringWalker(String toWalk)
	{
		this.walked = toWalk;
		atLineStart = true;
		cursor = -1;
		fetchNextCharacter();
		current = next;
		cursor++;
		fetchNextCharacter();
		updateAtLineEnd();
	}

	/**
	 * True if there are more characters to walk through.
	 */
	public boolean hasMore()
	{
		return cursor < walked.length();
	}
	/**
	 * Advance by one character (two if these are a Windows line-ending CR+LF pair).
	 */
	public void forward()
	{
		if (hasMore())
		{
			atLineStart = atLineEnd;
			cursor++;
			previous = current;
			current = next;
			updateAtLineEnd();
			fetchNextCharacter();
		}
		else
		{
			atLineStart = false;
			atLineEnd = true;
			previous = current = next = PLACEHOLDER_CHAR;
		}
	}

	/**
	 * Advances by n characters (see {@link #forward()} remark on EOL).
	 */
	public void forward(int n)
	{
		for (int i = 0; i < n; i++)
		{
			forward();
		}
	}

	/**
	 * Goes forward, skipping whitespace characters (space & tab only).
	 *
	 * @return the number of whitespace characters that has been skipped
	 */
	public int skipSpaces()
	{
		int counter = 0;
		while (CharacterCheck.isWhitespace(current))
		{
			forward();
			counter++;
		}
		return counter;
	}

	public void goToNextLine()
	{
		do
		{
			forward();
		} while (!atLineStart && hasMore());
	}

	/**
	 * True if we are no an end-of-line character<br>
	 * (classical CR and LF, but also Unicode EOL code points).
	 */
	public boolean atLineEnd()
	{
		return atLineEnd;
	}
	/**
	 * True if we are at the start of a line (just after an EOL).
	 */
	public boolean atLineStart()
	{
		return atLineStart;
	}

	/**
	 * Returns the current character, if any (space otherwise).
	 */
	public char current()
	{
		return current;
	}
	/**
	 * Returns the previous character, if any (space otherwise).
	 */
	public char previous()
	{
		return previous;
	}
	/**
	 * Returns the next character, if any (space otherwise).
	 */
	public char next()
	{
		return next;
	}

	/**
	 * True if the current character is the given one.
	 */
	public boolean match(char c)
	{
		return c == current;
	}
	/**
	 * True if the current and next characters are the given ones.
	 */
	public boolean match(char c1, char c2)
	{
		return c1 == current && c2 == next;
	}
	/**
	 * True if the string at the current position matches the given string.
	 */
	public boolean match(String s)
	{
		if (s == null || s.isEmpty())
			return false; // Whatever...
		if (s.charAt(0) != current)
			return false;
		if (s.length() == 1)
			return true;
		if (s.charAt(1) != next)
			return false;
		if (s.length() == 2)
			return true;
		for (int i = 2; i < s.length(); i++)
		{
			if (s.charAt(i) != safeCharAt(cursor + i, PLACEHOLDER_CHAR))
				return false;
		}
		return true;
	}
	/**
	 * match() with forward offset relative to the position of the cursor.
	 */
	public boolean matchAt(int offset, String s)
	{
		if (s == null || s.isEmpty())
			return false; // Whatever...
		// Same algo without the quick exits
		for (int i = 0; i < s.length(); i++)
		{
			if (s.charAt(i) != safeCharAt(offset + cursor + i, PLACEHOLDER_CHAR))
				return false;
		}
		return true;
	}

	/**
	 * Returns the character at the given offset from the current character.
	 * Can check with {@link StringWalker#isValid(char)} if the character is a valid one
	 * (position outside the range of the string to walk).
	 * Note: can return a CR or LF character if going over the line boundary.
	 *
	 * @param pos  the position / index of the character to fetch
	 *
	 * @return the fetched character
	 */
	public char charAt(int position)
	{
		int pos = position + cursor;
		if (pos >= 0 && pos < walked.length())
			return walked.charAt(pos);

		return PLACEHOLDER_CHAR;
	}

	/**
	 * Tells if the given character is a valid one.
	 * "Valid" applies only to return values of {@link StringWalker#previous()}, {@link StringWalker#current()},
	 * {@link StringWalker#next()} or {@link StringWalker#charAt(int)}.
	 * These values are invalid if the corresponding position in the string is invalid (beyond its bounds).
	 *
	 * @param c  the character to check
	 * @return true if the character is valid, false otherwise
	 */
	public static boolean isValid(char c)
	{
		return c != PLACEHOLDER_CHAR;
	}

	private void fetchNextCharacter()
	{
		next = safeCharAt(cursor + 1, PLACEHOLDER_CHAR);
		if (current == '\r' && next == '\n')
		{
			// Skip the carriage return we have in Windows line breaks, so we standardize on the newline char.
			// Don't take in account old Mac (pre-OSX) end-of-line (carriage return only).
			next = safeCharAt(++cursor + 1, PLACEHOLDER_CHAR);
		}
	}
	/**
	 * Returns the character at the given position.
	 * If the position is outside the range of the string to walk, returns the given default character.
	 *
	 * @param pos  the position / index of the character to fetch
	 * @param defaultChar  the character to return if the position is invalid
	 * @return the fetched character or the default one
	 */
	private char safeCharAt(int pos, char defaultChar)
	{
		if (pos >= 0 && pos < walked.length())
			return walked.charAt(pos);

		return defaultChar;
	}

	private void updateAtLineEnd()
	{
		atLineEnd = CharacterCheck.isLineTerminator(current) || !hasMore();
	}

	@Override
	public String toString()
	{
		String position = "";
		position += atLineStart ? "line start" : "";
		position += atLineEnd ? "line end" : "";
		return "StringWalker[cursor=" + cursor + (position.isEmpty() ? "" : ", " + position) +
				", context=" + toString(previous) + toString(current) + toString(next) +
				", [" + (hasMore() ? walked.substring(cursor) : "") + "]]";
	}
	private String toString(char c)
	{
		if (c == PLACEHOLDER_CHAR)
			return "<none>";
		if (CharacterCheck.isLineTerminator(c))
			return "\\n";
		return Character.toString(c);
	}
}
