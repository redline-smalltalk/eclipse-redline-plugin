package st.redline.eclipse.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordPatternRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;

public class SmalltalkScanner extends RuleBasedScanner {

	private static final String[] KEYWORDS = { "true", "false", "nil", "self",
			"super", "thisContext" };
	private static final String[] OPERATORS = { ":=", "+", "-", "*", "<", ">" };

	public SmalltalkScanner(SmalltalkEditorColorManager colorManager) {
		Token keywordToken = new Token(new TextAttribute(
				colorManager.getColor(ISmalltalkColorConstants.KEYWORD), null,
				SWT.BOLD));
		Token symbolToken = new Token(new TextAttribute(
				colorManager.getColor(ISmalltalkColorConstants.SYMBOL)));
		Token stringToken = new Token(new TextAttribute(
				colorManager.getColor(ISmalltalkColorConstants.STRING)));
		Token numberToken = new Token(new TextAttribute(
				colorManager.getColor(ISmalltalkColorConstants.NUMBER)));
		Token commentToken = new Token(new TextAttribute(
				colorManager.getColor(ISmalltalkColorConstants.STRING), null,
				SWT.ITALIC));
		Token arrayToken = new Token(new TextAttribute(
				colorManager.getColor(ISmalltalkColorConstants.ARRAY)));
		Token blockToken = new Token(new TextAttribute(
				colorManager.getColor(ISmalltalkColorConstants.BLOCK)));
		Token operatorToken = new Token(new TextAttribute(
				colorManager.getColor(ISmalltalkColorConstants.OPERATOR)));

		WordRule keywordRule = new WordRule(new IWordDetector() {
			public boolean isWordStart(char c) {
				return Character.isJavaIdentifierStart(c);
			}

			public boolean isWordPart(char c) {
				return Character.isJavaIdentifierPart(c);
			}
		});

		// add tokens for each reserved word
		for (int n = 0; n < KEYWORDS.length; n++) {
			keywordRule.addWord(KEYWORDS[n], keywordToken);
		}

		WordRule symbolRule = new WordRule(new IWordDetector() {

			@Override
			public boolean isWordStart(char c) {
				return c == '#';
			}

			@Override
			public boolean isWordPart(char c) {
				return Character.isJavaIdentifierPart(c);
			}
		}, symbolToken);


		setRules(new IRule[] { 
				keywordRule, 
				symbolRule,
				new ParameterRule(":", blockToken),
				new SingleLineRule("#(", ")", arrayToken),
				new SingleLineRule("#[", "]", arrayToken),
				new SingleLineRule("\"", "\"", commentToken, '\\'),
				new SingleLineRule("'", "'", stringToken, '\\'),
				new WordRule(new NumberDetector(), numberToken),
				//new WordRule(new ObjectDetector(), numberToken),
				new WhitespaceRule(new IWhitespaceDetector() {
					public boolean isWhitespace(char c) {
						return Character.isWhitespace(c);
					}
				}), });
	}

	public static class ParameterRule extends WordPatternRule {

		public ParameterRule(String endSequence, IToken token) {
			super(new IWordDetector() {

				@Override
				public boolean isWordStart(char c) {
					return Character.isJavaIdentifierPart(c)
							&& Character.isLowerCase(c);
				}

				@Override
				public boolean isWordPart(char c) {
					return Character.isJavaIdentifierPart(c) || c == ':';
				}
			}, " ", endSequence, token);
		}

		@Override
		protected boolean sequenceDetected(ICharacterScanner scanner,
				char[] sequence, boolean eofAllowed) {
			// TODO Auto-generated method stub

			if (scanner.getColumn() == 0)
				return false;

			scanner.unread();

			int c = scanner.read();
			if (c != ' ') {
				return false;
			}

			c = scanner.read();

			if (!Character.isLetter(c) || !Character.isLowerCase(c)) {
				return false;
			}

			return true;
			// return super.sequenceDetected(scanner, sequence, eofAllowed);
		}
	}

	public static class NumberDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {

			return isNumber(c);
		}

		private boolean isNumber(char c) {
			return c >= '0' && c <= '9';
		}

		@Override
		public boolean isWordPart(char c) {
			return isNumber(c);
		}

	}
	
	public static class ObjectDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {

			return Character.isUpperCase(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isJavaLetter(c);
		}

	}
}
