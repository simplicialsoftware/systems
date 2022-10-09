package simplicial.software.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import simplicial.software.Systems.R;

public class HelpActivity extends Activity implements OnClickListener {
    Button bClose;
    TextView tvHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        bClose = findViewById(R.id.bClose);
        bClose.setOnClickListener(this);

        tvHelp = findViewById(R.id.tvHelp);
        tvHelp.setText(Html.fromHtml(getString(R.string.help)));
        tvHelp.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v == bClose) {
            finish();
        }
    }
}
