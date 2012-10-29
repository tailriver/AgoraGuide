package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CreditsActivity extends AgoraActivity {
	private int padding;

	@Override
	public void onPreInitialize() {
		padding = getResources().getDimensionPixelSize(R.dimen.contentPadding);

		LinearLayout root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setPadding(padding, padding, padding, padding);

		TextView linkLabel = new TextView(this);
		linkLabel.setText("リンク");
		linkLabel.setPadding(0, 0, 0, padding);
		root.addView(linkLabel);

		List<Pair<String, String>> links = new ArrayList<Pair<String, String>>();
		links.add(Pair.create("サイエンスアゴラ2012", "http://www.scienceagora.org/"));
		links.add(Pair.create("日本科学未来館", "http://www.miraikan.jst.go.jp/"));
		links.add(Pair.create("産業技術総合研究所", "http://www.aist.go.jp/"));
		links.add(Pair.create("東京都立産業技術総合研究センター", "http://www.iri-tokyo.jp/"));
		addLinkCredits(root, links);

		TextView acknowledgementLabel = new TextView(this);
		acknowledgementLabel.setText("謝辞");
		acknowledgementLabel.setPadding(0, 2 * padding, 0, padding);
		root.addView(acknowledgementLabel);

		List<Pair<String, String>> acknowledgements = new ArrayList<Pair<String, String>>();
		acknowledgements.add(Pair.create("独立行政法人 科学技術振興機構", "http://www.jst.go.jp/"));
		addLinkCredits(root, acknowledgements);

		TextView copyrightLabel = new TextView(this);
		copyrightLabel.setText("著作権情報");
		copyrightLabel.setPadding(0, 2 * padding, 0, padding);
		root.addView(copyrightLabel);

		TableLayout copyrightTable = new TableLayout(this);
		copyrightTable.setPadding(2 * padding, 0, 0, 0);
		copyrightTable.setColumnShrinkable(1, true);
		List<Pair<String[], String>> copyrights = new ArrayList<Pair<String[], String>>();
		copyrights.add(Pair.create(
				new String[]{"企画情報", "会場マップ"},
				"© 2012 JST"));
		copyrights.add(Pair.create(
				new String[]{"ロゴ", "イラスト"},
				"© 2011-2012 青木風人、長田絵理香、堀内瑶恵、田中佐代子（筑波大学）"));
		copyrights.add(Pair.create(
				new String[]{"アプリ"},
				"© 2011-2012 Shinsuke Ogawa"));
		addCopyrightCredits(copyrightTable, copyrights);
		root.addView(copyrightTable);

		ScrollView wrapper = new ScrollView(this);
		wrapper.addView(root);
		setTitle(R.string.credits);
		setContentView(wrapper);
	}

	@Override
	public void onPostInitialize(Bundle savedInstanceState) {
	}

	private void addLinkCredits(ViewGroup parent, List<Pair<String, String>> list) {
		for (final Pair<String, String> p : list) {
			TextView link = new TextView(this);
			link.setText(getLinkSpannable(p.first));
			link.setPadding(2 * padding, 0, 0, 0);
			link.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(p.second));
					startActivity(intent);
				}
			});
			parent.addView(link);
		}
	}

	private void addCopyrightCredits(ViewGroup parent, List<Pair<String[], String>> list) {
		for (final Pair<String[], String> p : list) {
			TableRow row = new TableRow(this);
			TextView left  = new TextView(this);
			TextView right = new TextView(this);

			StringBuilder leftText = new StringBuilder();
			String delimiter = isLandscape() ? "・" : "\n";
			for (String s : p.first) {
				leftText.append(s).append(delimiter);
			}
			left.setText(leftText.substring(0, leftText.length() - delimiter.length()));
			left.setTextColor(Color.DKGRAY);
			left.setPadding(0, 0, padding, 0);
			right.setText(p.second);

			row.addView(left);
			row.addView(right);
			parent.addView(row);
		}
	}

	private CharSequence getLinkSpannable(String string) {
		SpannableString ss = new SpannableString(string);
		ss.setSpan(new ForegroundColorSpan(Color.BLUE),
				0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		ss.setSpan(new UnderlineSpan(),
				0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ss;
	}
}
