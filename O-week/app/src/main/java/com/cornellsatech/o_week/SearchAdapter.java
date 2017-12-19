package com.cornellsatech.o_week;

import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cornellsatech.o_week.models.Event;

import java.lang.annotation.Retention;
import java.util.Collections;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Display search results under two headers: My Calendar & All Events
 */

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @Retention(SOURCE)
    @IntDef({HEADER_VIEW_FIRST, HEADER_VIEW_SECOND, CONTENT_VIEW})
    public @interface ViewType {}
    private final static int HEADER_VIEW_FIRST = 0;
    private final static int HEADER_VIEW_SECOND = 1;
    private final static int CONTENT_VIEW = 2;
    private List<Event> events = Collections.emptyList();
    private List<Integer> headerCellIndexes = Collections.emptyList();
    private static final String TAG = SearchAdapter.class.getSimpleName();

    /**
     * Points events list & headers cell indexes to given parameters
     * Note: Don't mutate parameters
     * @param events  Events list
     * @param headerCellIndexes Hader cell indexes list
     */
    public void updateEventsAndHeaders(List<Event> events, List<Integer> headerCellIndexes) {
        this.events = events;
        this.headerCellIndexes = headerCellIndexes;
        notifyDataSetChanged();
    }

    /**
     * Depending on type, returns a {@link HeaderCell} or {@link FeedCell} to be displayed
     *
     * @param parent
     * @param viewType First header view, second header view, or content view
     * @return {@link FeedCell} or {@link HeaderCell}
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @ViewType int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case HEADER_VIEW_FIRST:
                return new HeaderCell(inflater.inflate(R.layout.cell_header, parent, false));
            case HEADER_VIEW_SECOND:
                return new HeaderCell(inflater.inflate(R.layout.cell_header, parent, false));
            case CONTENT_VIEW:
                return new FeedCell(inflater.inflate(R.layout.cell_feed, parent, false));
            default:
                return null;
        }
    }


    /**
     * Return correct type based on position
     * @param position position of cell
     * @return integer associated with the type of cell (Header or Feed cell)
     */
    @Override
    @ViewType
    public int getItemViewType(int position) {
        int headerIndexMin = Collections.min(headerCellIndexes);
        int headerIndexMax = Collections.max(headerCellIndexes);
        if (position == headerIndexMin) {
            return HEADER_VIEW_FIRST;
        } else if (position == headerIndexMax) {
            return HEADER_VIEW_SECOND;
        } else {
            return CONTENT_VIEW;
        }
    }

    /**
     * Based on type, configures Header cell with correct title or Feed cell
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch(viewType) {
            case HEADER_VIEW_FIRST:
                HeaderCell headerCell = (HeaderCell) viewHolder;
                headerCell.configure(true);
                break;
            case HEADER_VIEW_SECOND:
                HeaderCell headerCell2 = (HeaderCell) viewHolder;
                headerCell2.configure(false);
                break;
            case CONTENT_VIEW:
                Event event = events.get(position);
                FeedCell feedCell = (FeedCell) viewHolder;
                feedCell.configure(event);
                break;
            default:
                break;
        }
    }

    /**
     * Returns total number of cells (# of {@link #events})
     * that will be shown in the {@link RecyclerView}.
     * Note: {@link #events} list is the length of all events + header cells
     * @return Total number of cells
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

}
