package harris.GiantBomb;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class BombcastList extends ListActivity {
	private ArrayList<News> news;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newslist);
		loadFeed();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.get(
				position).getLink()));
		startActivity(myIntent);
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, 1, 0, "Share");
		menu.add(0, 2, 0, "Download");
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		if (item.getItemId() == 1) {
			System.out.println(info.id);
			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, news.get((int) info.id)
					.getLink());
			startActivity(Intent.createChooser(shareIntent,
					"Share link with..."));
		} else if (item.getItemId() == 2) {
			Intent myIntent = new Intent(this, DownloadView.class);
			Bundle bundle = new Bundle();
			bundle.putString("URL", news.get((int) info.id).getLink());
			bundle.putString("title", news.get((int) info.id).getTitle());
			myIntent.putExtras(bundle);
			this.startActivity(myIntent);
		}
		return super.onContextItemSelected(item);
	}

	@SuppressWarnings("unchecked")
	private void loadFeed() {
		final ListActivity list = this;
		final ProgressDialog dialog = ProgressDialog.show(BombcastList.this,
				"", "Loading. Please wait...", true);
		dialog.show();

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				dialog.dismiss();
				list.setListAdapter(((ArrayAdapter) message.obj));
				registerForContextMenu(getListView());
			}
		};

		Thread thread = new Thread() {
			@Override
			public void run() {

				try {
					NewsFeedParser parser = new NewsFeedParser(
							"http://www.giantbomb.com/podcast-xml/");
					news = (ArrayList<News>) parser.parse();
					Message message;
					message = handler.obtainMessage(-1,
							new BombcastListAdapter(list, R.layout.newsrow,
									news));
					handler.sendMessage(message);
				} catch (Throwable t) {
				}
			}
		};
		thread.start();
	}

}
